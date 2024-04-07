#include <stdio.h>

int add(int a, int b) {
  printf("hello function pointer\n");
  return a + b;
}

int main() {
  int (*func)(int, int) = add;
  func(1, 2);

  return 0;
}