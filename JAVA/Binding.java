//202121224 강지웅
class Animal // 클래스 정의
{
    void makeSound() // 함수
    {
        System.out.println("동물 소리");
    }
}

class Dog extends Animal // 상속받는 클래스
{
    @Override
    void makeSound() // 함수
    {
        System.out.println("멍멍");
    }
}


public class Binding
{
    public static void main(String[] args) {
        Animal A = new Dog(); // A는 Animal 타입이지만, 인스턴스는 Dog객체임
        A.makeSound(); // 동적바인딩이 일어난 문장
    }
}
