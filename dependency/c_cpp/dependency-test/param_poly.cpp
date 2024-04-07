#include <stdio.h>

template<typename T>
void foo(T t) {
  printf("hello poly\n");
}

int main() {
  foo(100);
  foo(2.2);

  return 0;
}