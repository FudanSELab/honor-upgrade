class Shape {
    public void draw() {
        // System.out.println("Drawing a shape");
    }
}

class Circle extends Shape {
    @Override
    public void draw() {
        // System.out.println("Drawing a circle");
    }
}

class Triangle extends Shape {
    @Override
    public void draw() {
        // System.out.println("Drawing a triangle");
    }
}

class Main {
    public static void main(String[] args) {
        Shape shape1 = new Circle();
        Shape shape2 = new Triangle();

        if (args[1].equals("jk")) {
          shape1 = new Triangle();
        }
        
        shape1.draw();
        // shape2.draw();
    }
}