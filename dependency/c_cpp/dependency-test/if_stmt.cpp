// test function call in if-stmt



#include <stdio.h>

void foo() {
  printf("hello foo\n");
}

void goo() {
  printf("hello goo\n");
}

int main() {
  // if statement
  if (true) {
    foo();
  } else {
    goo();
  }

  return 0;
}