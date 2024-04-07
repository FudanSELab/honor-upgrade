#include <stdio.h>

int main() {
  auto lamb = [](int a, int b) {
    printf("hello lambda\n");
    return a + b;
  };

  auto c = lamb(1, 2);

  return 0;
}