// test function call in while-stmt



#include <stdio.h>

void foo() {
  printf("hello foo\n");
}

void goo() {
  printf("hello goo\n");
}

int main() {
  // while statement
  while (false) {
    foo();
  }
  goo();

  return 0;
}