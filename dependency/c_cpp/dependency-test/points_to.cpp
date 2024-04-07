class C {
public:
  C *f;
};

int main() {
  auto b = new C{};
  // b = {line 7 * C}
  auto a = b;
  // a = {line 7 * C}, b = {line 7 * C}
  auto c = new C{};
  // a = {line 7 * C}, b = {line 7 * C}, c = {line 11, C}
  c->f = a;
  // a = {line 7 * C}, b = {line 7 * C}, c = {line 11, C},
  // {line 10 * C}.f = {line 7 * C}
  auto d = c;
  // a = {line 7 * C}, b = {line 7 * C}, c = {line 11 * C},
  // {line 11 * C}.f = {line 7 * C}, d = {line 11 * C}
  c->f = d;
  // a = {line 7 * C}, b = {line 7 * C}, c = {line 11 * C},
  // {line 11 * C}.f = {line 11 * C}, d = {line 11 * C}
  auto e = d->f;
  // a = {line 7 * C}, b = {line 7 * C}, c = {line 11 * C},
  // {line 11 * C}.f = {line 11 * C}, d = {line 11 * C},
  // e = {line 10 * C}
}