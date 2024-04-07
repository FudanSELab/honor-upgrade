#include <stdio.h>

void foo(int,int) {
  printf("void foo(int,int)\n");
}

void foo(float, bool) {
  printf("void foo(float, bool)\n");
}

int main() {
  foo(1, 2);
  foo(2.1, false);

  return 0;
}