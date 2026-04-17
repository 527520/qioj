# 简单的 Python 计算程序
import sys

def main():
    # 读取输入参数
    if len(sys.argv) > 1:
        a = int(sys.argv[1])
        b = int(sys.argv[2])
        print(a + b)
    else:
        # 从标准输入读取
        line = sys.stdin.readline().strip()
        if line:
            numbers = line.split()
            if len(numbers) >= 2:
                a = int(numbers[0])
                b = int(numbers[1])
                print(a + b)

if __name__ == "__main__":
    main()