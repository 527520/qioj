#include <iostream>

int main() {
    int num1, num2, sum;
    std::cout << "请输入第一个整数: ";
    std::cin >> num1;
    std::cout << "请输入第二个整数: ";
    std::cin >> num2;
    sum = num1 + num2;
    std::cout << "两个整数之和为: " << sum << std::endl;
    return 0;
}
