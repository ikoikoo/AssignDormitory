import pandas as pd
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.cluster import KMeans
import argparse
import sys # 用于退出脚本
import os
import numpy as np # 引入 numpy 以便处理潜在的 NaN 或 Inf

# --- 配置 (可以根据需要调整) ---
TARGET_DORM_SIZE = 5  # 目标宿舍大小 (例如 4-6 人的平均值)
RANDOM_STATE = 42    # KMeans 的随机种子，确保结果可复现
N_INIT = 'auto'      # KMeans 运行次数，'auto' 通常对应 10 次

# --- 命令行参数解析 ---
parser = argparse.ArgumentParser(description='Perform K-Means dorm assignment based on user data.')
# --input: 指定输入的 CSV 文件路径，必需
parser.add_argument('--input',         # 参数名，前面有 --
                    required=True,    # 设置为必需
                    help='Path to the input CSV file containing user data.') # 帮助信息

# 定义 --output 参数 ---
parser.add_argument('--output',        # 参数名，前面有 --
                    required=True,    # 设置为必需
                    help='Path to save the output CSV file with assignments.') # 帮助信息args = parser.parse_args()

args = parser.parse_args()
output_file = args.output

input_file = args.input   # 通过 args.参数名 获取值
output_file = args.output # 通过 args.参数名 获取值

print(f"Python: Script started.")
print(f"Python: Input file path: {input_file}")
print(f"Python: Output file path: {output_file}")

# --- 读取数据 ---
print("Python: Attempting to read input file...")
try:
    expected_columns = ['user_id', 'gender', 'mbti_type', 'routine_score', 'hygiene_score', 'social_score'] # 只读取需要的列
    # 指定数据类型，特别是 user_id 应该是整数，分数可以是浮点数（如果标准化后）
    dtype_spec = {
        'user_id': int,
        'gender': str,
        'mbti_type': str,
        'routine_score': float, # 使用 float 以便后续处理
        'hygiene_score': float,
        'social_score': float
    }
    df = pd.read_csv(input_file, usecols=expected_columns, dtype=dtype_spec, keep_default_na=True, na_values=['', '#N/A', '#N/A N/A', '#NA', '-1.#IND', '-1.#QNAN', '-NaN', '-nan', '1.#IND', '1.#QNAN', '<NA>', 'N/A', 'NA', 'NULL', 'NaN', 'None', 'nan', 'null'])
    # keep_default_na=True, na_values=... 明确处理各种可能的空值表示

    print(f"Python: Successfully read {len(df)} records.")

    if df.empty:
        print("Python: Input data is empty. Exiting and creating empty output file.")
        # 创建一个空的输出文件并退出
        pd.DataFrame(columns=['user_id', 'dorm_id']).to_csv(output_file, index=False)
        sys.exit(0) # 正常退出

except FileNotFoundError:
    print(f"Python Error: Input file not found at {input_file}")
    sys.exit(1) # 异常退出
except KeyError as e:
    print(f"Python Error: Column not found in input CSV: {e}. Please ensure the CSV contains columns: {expected_columns}")
    sys.exit(1)
except Exception as e:
    print(f"Python Error: Failed to read input file: {e}")
    sys.exit(1)

# --- 数据预处理 ---
print("Python: Starting data preprocessing...")
try:
    # 1. 处理缺失值 (非常重要!)
    # 分类特征填充: 用一个明确的类别如 'Unknown'
    categorical_features = ['gender', 'mbti_type']
    df[categorical_features] = df[categorical_features].fillna('Unknown')
    print(f"Python: Missing values in categorical features filled with 'Unknown'.")

    # 数值特征填充: 可以用均值、中位数或 0，这里用 0 作为示例（因为分数通常非负）
    numerical_features = ['routine_score', 'hygiene_score', 'social_score']
    # 计算填充值（例如，每列的均值，忽略NaN）
    fill_values = df[numerical_features].mean()
    print(f"Python: Calculated fill values for numerical features: {fill_values.to_dict()}")
    df[numerical_features] = df[numerical_features].fillna(fill_values)
    # 再次检查是否还有NaN (如果均值也是NaN，说明整列都是NaN)
    if df[numerical_features].isnull().values.any():
        print(f"Python Warning: Filling with mean resulted in NaN. Filling remaining NaNs with 0.")
        df[numerical_features] = df[numerical_features].fillna(0)

    print(f"Python: Missing values in numerical features filled.")


    # 2. 独热编码 (One-Hot Encoding)
    print("Python: Performing One-Hot Encoding...")
    # 定义所有可能的类别，确保一致性（需要与 Java 端定义一致）
    # 注意要包含填充的 'Unknown'
    gender_cats = ['男', '女', '其他', 'Unknown'] # 根据你的实际情况调整
    mbti_cats = [ # 所有 16 种 + Unknown
        'ISTJ', 'ISFJ', 'INFJ', 'INTJ', 'ISTP', 'ISFP', 'INFP', 'INTP',
        'ESTP', 'ESFP', 'ENFP', 'ENTP', 'ESTJ', 'ESFJ', 'ENFJ', 'ENTJ', 'Unknown'
    ]
    encoder = OneHotEncoder(categories=[gender_cats, mbti_cats], sparse_output=False, handle_unknown='ignore') # 使用预定义类别
    encoded_cats = encoder.fit_transform(df[categorical_features])
    # 获取编码后的特征名称
    encoded_cat_cols = encoder.get_feature_names_out(categorical_features)
    encoded_cat_df = pd.DataFrame(encoded_cats, columns=encoded_cat_cols, index=df.index) # 保持索引一致
    print(f"Python: One-Hot Encoding complete. Encoded columns: {encoded_cat_cols.tolist()}")


    # 3. 标准化数值特征 (Standard Scaling - Z-score)
    print("Python: Performing Standard Scaling on numerical features...")
    scaler = StandardScaler()
    # 确保只对数值列操作，并且没有 NaN 或 Inf
    num_data_for_scaling = df[numerical_features].replace([np.inf, -np.inf], np.nan).fillna(0) # 替换 Inf 并填充 NaN
    scaled_nums = scaler.fit_transform(num_data_for_scaling)
    scaled_num_df = pd.DataFrame(scaled_nums, columns=numerical_features, index=df.index) # 保持索引一致
    print("Python: Standard Scaling complete.")


    # 4. 合并所有特征
    print("Python: Merging features...")
    # 删除原始的分类和数值列，用编码和标准化后的列替换
    df_to_cluster = df.drop(columns=categorical_features + numerical_features)
    # 合并编码后的分类特征和标准化后的数值特征
    df_processed = pd.concat([df_to_cluster, encoded_cat_df, scaled_num_df], axis=1)

    # 提取用于聚类的特征矩阵 (不包含 user_id)
    feature_matrix = df_processed.drop(columns=['user_id']).values
    print(f"Python: Preprocessing complete. Feature matrix shape: {feature_matrix.shape}")

    # 检查特征矩阵中是否还包含 NaN 或 Inf (重要!)
    if np.isnan(feature_matrix).any() or np.isinf(feature_matrix).any():
        print("Python Error: Feature matrix contains NaN or Inf after preprocessing! Cannot proceed with KMeans.")
        # 可以尝试再次填充或打印出问题行/列进行调试
        # print(df_processed[df_processed.drop(columns=['user_id']).isnull().any(axis=1)])
        sys.exit(1)

except Exception as e:
    print(f"Python Error: Data preprocessing failed: {e}")
    import traceback
    traceback.print_exc() # 打印详细错误堆栈
    sys.exit(1)


# --- K-Means 聚类 ---
print("Python: Starting K-Means clustering...")
try:
    n_users = len(feature_matrix) # 使用特征矩阵的行数
    if n_users == 0:
        print("Python Error: No data available for clustering after preprocessing.")
        sys.exit(1)

    # 计算 K 值
    if n_users < TARGET_DORM_SIZE:
        k = 1
        print(f"Python: Number of users ({n_users}) is less than target dorm size. Setting k = 1.")
    else:
        k = round(n_users / TARGET_DORM_SIZE)
        k = max(1, k) # 保证 k 至少为 1
    # 防止 K 大于样本数
    if k > n_users:
        print(f"Python Warning: Calculated k ({k}) is greater than number of users ({n_users}). Setting k = {n_users}.")
        k = n_users

    print(f"Python: Calculated number of clusters (k): {k}")

    if k <= 0:
        print("Python Error: Invalid number of clusters (k <= 0). Cannot perform KMeans.")
        sys.exit(1)

    # 执行 KMeans
    # random_state 确保结果可复现
    kmeans = KMeans(n_clusters=k, random_state=RANDOM_STATE, n_init=N_INIT)
    cluster_labels = kmeans.fit_predict(feature_matrix)
    print(f"Python: K-Means clustering complete. Assigned labels for {len(cluster_labels)} users.")

except ValueError as e:
    print(f"Python Error: K-Means failed. Check if k ({k}) is valid for the number of samples ({n_users}). Error: {e}")
    sys.exit(1)
except Exception as e:
     print(f"Python Error: K-Means failed: {e}")
     import traceback
     traceback.print_exc()
     sys.exit(1)


# --- 准备并保存输出结果 ---
print("Python: Preparing and saving output file...")
try:
    # 创建包含 user_id 和 分配结果 (dorm_id) 的 DataFrame
    # 确保使用原始 DataFrame 中的 user_id，其顺序应与 feature_matrix 的行顺序一致
    results_df = pd.DataFrame({
        'user_id': df['user_id'].values, # 使用 .values 确保是 NumPy 数组
        'dorm_id': cluster_labels + 1    # 将簇标签 (0-based) + 1 作为宿舍号 (1-based)
    })

    # 保存结果到指定的输出 CSV 文件，不包含索引列
    results_df.to_csv(output_file, index=False, encoding='utf-8') # 指定 UTF-8 编码
    print(f"Python: Assignment results successfully saved to {output_file}")

except Exception as e:
     print(f"Python Error: Failed to prepare or save output file: {e}")
     import traceback
     traceback.print_exc()
     sys.exit(1)

print("Python: Script finished successfully.")
sys.exit(0) # 显式以成功状态退出