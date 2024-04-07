class Apple {
public:
  Apple* f;
};

int main() {
  auto b = new Apple;
  auto a = b;
  auto c = new Apple;
  c->f = a;
  auto d = c;
  c->f = d;
  auto e = d->f;
  return 0;

  // b = 7
  // a = 7
  // c = 9
  // d = 9
  // c.f = 9
  // e = 9
}