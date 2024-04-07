// test method apply

#include <stdio.h>

class Animal {
  int age;
  
};

class Cat : public Animal {
public:
  int name;
  void foo() {
    printf("hello cat?");
  }
};

int main() {
  auto cat = Cat{};
  cat.foo();
}