#include <stdio.h>

class A {
public:
  virtual void foo() {
    printf("A::foo()\n");
  }

  void goo() {
    printf("A::goo()\n");
  }
};

class AA : public A {
public:
  virtual void foo() override {
    printf("AA::foo()\n");
  }

  void goo() {
    printf("AA::goo()\n");
  }
};

class AB : public A {
public:
  virtual void foo() override {
    printf("AB::foo()\n");
  }

  void goo() {
    printf("A::goo()\n");
  }
};

int main() {
  A* a0 = new AA{};
  a0->foo();
  // AA::foo()

  A a1 = AA{};
  a1.foo();
  // different from a0->foo(), which call by virtual table

  A* a2 = new AA{};
  a2->foo();
  // AA::foo()
  a2->goo();
  // A::goo()

  return 0;
}