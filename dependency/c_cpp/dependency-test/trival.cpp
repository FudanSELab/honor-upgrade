// test the trival calling function



#include <stdio.h>

void foo() {
  printf("hello dependency\n");
}

int main() {
  foo();
  return 0;
}