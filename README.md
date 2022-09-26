# CS211 - Lab 11-12 Dictionary

ส่งงานที่ https://classroom.github.com/a/zFxVAMJK

ให้นิสิตสร้าง JavaFX Application สำหรับเพิ่มคำศัพท์ และความหมายในพจนานุกรม

1. มี UI หน้าแสดงคำศัพท์ทั้งหมดในคลังคำศัพท์ ที่สามารถเลือกคำศัพท์แล้วแสดงชนิดของคำศัพท์ ความหมายของคำศัพท์ และตัวอย่างประโยคจากคำศัพท์
1. มี UI หน้าเพิ่มคำศัพท์ ที่ระบุคำศัพท์ ชนิดของคำศัพท์ (parts of speech) ความหมายของคำศัพท์ และตัวอย่างประโยคจากคำศัพท์ (เพิ่มได้หลายประโยค)
1. ใช้ Collection and Map ในการเก็บคำศัพท์ในโปรแกรม
1. ใช้การอ่านเขียนไฟล์ในการเก็บคำศัพท์เพื่อให้การเปิดโปรแกรมครั้งถัดไปมีคำศัพท์
1. push ที่ Github Classroom

## What I Have Done and Learned

- A ___free___ version of __naiithink's Dictionary__[^1]
- Generics
    - Type bounding

        ```java
        Type<T extends Class<?>>
        ```
    - Wildcards

        Upper bound

        ```java
        <? extends Class<?>>
        ```
        Lower bound

        ```java
        <? super Class<?>>
        ```
- The relationship of generics

    - Covariance (more specific)
    - Contravariance (less specific)
    - Invariance (truely specific)
- PECS - Producer Extends Consumer Super

    ```java
    class Cat {

        private String name;

        public Cat(String name) {
            this.name = new String(name);
        }

        // ...
    }

    class Kitten extends Cat {

        public Kitten(String name) {
            super(name);
        }

        // ...
    }

    List<Kitten> kittens = new ArrayList<>();
    List<? extends Cat> cats = kittens;

    cats.add(new Cat("eiei"));    // compile-time error
    ```

    We cannot add a new `Cat` to the list of `Kitten`.  
    `List<Kitten>` is a subtype of `List<? extends Cat>`.
- The collection framework

    ```mermaid
    classDiagram

    Iterable <|-- Collection
    Collection <|-- List
    Collection <|-- Set

    <<interface>> Iterable
    <<interface>> Collection
    <<interface>> List
    <<interface>> Set

    class Map {
        <<interface>>
    }
    ```

    - `java.util.Map<K, V>` and its subtypes
    - `java.util.List<E>` and its subtypes
    - `java.util.Set<E>` and its subtypes
- Lambda expressions and `@FunctionaInterface`

    An interface with only 1 abstract method.

    - `java.util.function.Function<T, R>`
    - `java.util.function.Predicate<T>`
    - `java.util.function.Supplier<T>`
    - `java.util.function.Consumer<T>`
- `java.util.Optional<T>` class
- `java.lang.Thread` class
- `java.util.concurrent` package
- Thread safety
- Design patterns
    - Observer (events)
    - Singleton
- Type safety and reflection
- Improving to make it more universal - [naiithink's StageController](src/main/java/com/github/naiithink/app/controllers/StageController.java)

## Experimental Features in Dev

- [ ] Data manipulations [Relational database](src/main/java/com/github/naiithink/app/experimental/services/Database.java)
- [ ] MVC::Model [Word with multiple entries, based on grammatical classes, aka part of speech](src/main/java/com/github/naiithink/app/experimental/models/Word.java)
- [ ] Reflection [Annotation to check if passed argument is an instance of a certain class, and other different contexts](src/main/java/com/github/naiithink/app/experimental/controllers/MainAppObject.java)
- [ ] IO [Dynamic file resource manipulations](src/main/java/com/github/naiithink/app/experimental/helpers/FileResource.java)


[^1]: Currently, I have no plan to develop any other versions of this app.
