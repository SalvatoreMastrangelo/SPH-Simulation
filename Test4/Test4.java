class Book {
    String title;
    int pages;

    public void read() {
        System.out.println("Reading " + title + " which has " + pages + " pages.");
    }
}

public class Test4 {
    public static void main(String[] args) {
        Book book = new Book();
        book.title = "The Hobbit";
        book.pages = 300;

        book.read();
    }
    
}
