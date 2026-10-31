package com.examination.config;

import com.examination.entity.*;
import com.examination.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final SubmissionRepository submissionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           SubjectRepository subjectRepository,
                           ExamRepository examRepository,
                           QuestionRepository questionRepository,
                           SubmissionRepository submissionRepository,
                           StudentAnswerRepository studentAnswerRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.submissionRepository = submissionRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return; // Data already initialized
        }

        System.out.println(">>> Initializing Online Examination System sample dataset...");

        // 1. Create Admin
        User admin = new User("System Administrator", "admin@exam.com", passwordEncoder.encode("admin123"), Role.ADMIN);
        userRepository.save(admin);

        // 2. Create Teachers
        User teacher1 = new User("Dr. Sarah Jenkins", "sarah.teacher@exam.com", passwordEncoder.encode("teacher123"), Role.TEACHER);
        User teacher2 = new User("Prof. Robert Vance", "robert.teacher@exam.com", passwordEncoder.encode("teacher123"), Role.TEACHER);
        userRepository.save(teacher1);
        userRepository.save(teacher2);

        // 3. Create Students
        User student1 = new User("Alice Johnson", "alice.student@exam.com", passwordEncoder.encode("student123"), Role.STUDENT);
        User student2 = new User("Bob Smith", "bob.student@exam.com", passwordEncoder.encode("student123"), Role.STUDENT);
        User student3 = new User("Carol White", "carol.student@exam.com", passwordEncoder.encode("student123"), Role.STUDENT);
        User student4 = new User("David Miller", "david.student@exam.com", passwordEncoder.encode("student123"), Role.STUDENT);
        User student5 = new User("Emma Davis", "emma.student@exam.com", passwordEncoder.encode("student123"), Role.STUDENT);
        userRepository.saveAll(List.of(student1, student2, student3, student4, student5));

        // 4. Create Subjects
        Subject sub1 = new Subject("Java Programming & OOP Design", "Core Java language concepts, Object-Oriented principles, JVM memory internals, Streams, and Multithreading.");
        Subject sub2 = new Subject("Database Management Systems (DBMS & MySQL)", "Relational database modeling, SQL DDL/DML, normalization, indexing structures, and ACID transactions.");
        Subject sub3 = new Subject("Web Technologies & Spring Boot Architecture", "Modern web protocols, REST API design, Spring MVC pattern, Thymeleaf templates, and security boundaries.");
        subjectRepository.saveAll(List.of(sub1, sub2, sub3));

        // 5. Create Exam 1: Java OOP Mastery
        Exam exam1 = new Exam(
                "Java Programming & OOP Foundations Exam",
                "Comprehensive assessment covering object-oriented programming, inheritance, polymorphism, Java collections, and exception handling principles.",
                sub1,
                teacher1,
                20, // 20 mins
                10, // 10 marks
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().plusDays(30),
                "PUBLISHED"
        );
        examRepository.save(exam1);

        // Exam 1 Questions (10 questions, 1 mark each)
        List<Question> exam1Questions = List.of(
                new Question(exam1, "Which of the following is NOT a fundamental pillar of Object-Oriented Programming?",
                        "Encapsulation", "Compilation", "Polymorphism", "Abstraction", "B", 1),
                new Question(exam1, "Which collection class in Java allows unique elements and maintains insertion order?",
                        "HashSet", "TreeSet", "LinkedHashSet", "ArrayList", "C", 1),
                new Question(exam1, "What happens if a class implements an interface but does not define all its abstract methods?",
                        "The class must be declared abstract", "It compiles without issues", "Runtime ClassCastException is thrown", "It causes a JVM crash", "A", 1),
                new Question(exam1, "What is the return type of the hashCode() method in Java Object class?",
                        "long", "int", "void", "String", "B", 1),
                new Question(exam1, "Which keyword is used to prevent method overriding in Java?",
                        "static", "abstract", "final", "synchronized", "C", 1),
                new Question(exam1, "In Java memory management, where are objects dynamically allocated?",
                        "Stack Memory", "Heap Memory", "Code Segment", "Register Memory", "B", 1),
                new Question(exam1, "Which interface is the root of the Java Collections Framework hierarchy (excluding Maps)?",
                        "List", "Set", "Collection", "Iterable", "D", 1),
                new Question(exam1, "What is the time complexity of searching an element in a balanced Binary Search Tree (AVL/Red-Black)?",
                        "O(1)", "O(n)", "O(log n)", "O(n log n)", "C", 1),
                new Question(exam1, "Which exception is thrown when an application attempts to use null where an object is required?",
                        "NullPointerException", "IllegalArgumentException", "IllegalStateException", "ClassNotFoundException", "A", 1),
                new Question(exam1, "What is the purpose of the 'transient' keyword in Java?",
                        "Marks a variable as thread-safe", "Prevents a field from being serialized", "Enables constant memory caching", "Makes a field immutable", "B", 1)
        );
        questionRepository.saveAll(exam1Questions);

        // 6. Create Exam 2: Database Systems & MySQL
        Exam exam2 = new Exam(
                "Database Systems & MySQL Architecture Exam",
                "In-depth examination testing SQL query writing, transaction isolation levels, B-Tree indexes, and normal forms in relational databases.",
                sub2,
                teacher2,
                25, // 25 mins
                10, // 10 marks
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(45),
                "PUBLISHED"
        );
        examRepository.save(exam2);

        // Exam 2 Questions (10 questions, 1 mark each)
        List<Question> exam2Questions = List.of(
                new Question(exam2, "In ACID transaction properties, what does the letter 'I' stand for?",
                        "Integrity", "Isolation", "Inheritance", "Immutability", "B", 1),
                new Question(exam2, "Which SQL command is used to remove all records from a table without logging individual row deletions?",
                        "DELETE", "DROP", "TRUNCATE", "REMOVE", "C", 1),
                new Question(exam2, "What normal form eliminates transitive functional dependencies?",
                        "First Normal Form (1NF)", "Second Normal Form (2NF)", "Third Normal Form (3NF)", "Boyce-Codd Normal Form (BCNF)", "C", 1),
                new Question(exam2, "Which data structure is primarily used by MySQL InnoDB for table indexes?",
                        "Hash Table", "B+ Tree", "Binary Heap", "Red-Black Tree", "B", 1),
                new Question(exam2, "Which SQL clause is used to filter records resulting from a GROUP BY aggregation?",
                        "WHERE", "HAVING", "LIMIT", "ORDER BY", "B", 1),
                new Question(exam2, "Which constraint enforces uniqueness across a column while permitting a single NULL value in standard SQL?",
                        "PRIMARY KEY", "UNIQUE", "CHECK", "FOREIGN KEY", "B", 1),
                new Question(exam2, "What happens during a Dirty Read phenomenon in database concurrency?",
                        "A transaction reads uncommitted changes made by another transaction",
                        "A query returns two different row counts in the same transaction",
                        "A transaction reads updated rows from committed transactions only",
                        "Deadlock occurs between two competing locks", "A", 1),
                new Question(exam2, "Which JOIN returns all rows from the left table and matched rows from the right table?",
                        "INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL OUTER JOIN", "B", 1),
                new Question(exam2, "Which command is used to save all changes made by a transaction permanently?",
                        "ROLLBACK", "COMMIT", "SAVEPOINT", "CHECKPOINT", "B", 1),
                new Question(exam2, "What is a Foreign Key constraint primarily used for in relational schema design?",
                        "To sort query output quickly", "To enforce referential integrity between tables", "To partition large tables across disk drives", "To encrypt column values", "B", 1)
        );
        questionRepository.saveAll(exam2Questions);

        // 7. Seed Sample Submissions for Student Dashboard & Reports
        // Alice attempted Exam 1 -> score 9/10
        Submission subAlice = new Submission(student1, exam1, 9, LocalDateTime.now().minusHours(5), "COMPLETED");
        submissionRepository.save(subAlice);
        List<StudentAnswer> aliceAnswers = new ArrayList<>();
        for (int i = 0; i < exam1Questions.size(); i++) {
            Question q = exam1Questions.get(i);
            // Alice gets question index 4 wrong (selected 'A' instead of 'C'), rest correct
            String selected = (i == 4) ? "A" : q.getCorrectAnswer();
            aliceAnswers.add(new StudentAnswer(subAlice, q, selected));
        }
        studentAnswerRepository.saveAll(aliceAnswers);
        subAlice.setStudentAnswers(aliceAnswers);

        // Bob attempted Exam 1 -> score 7/10
        Submission subBob = new Submission(student2, exam1, 7, LocalDateTime.now().minusHours(2), "COMPLETED");
        submissionRepository.save(subBob);
        List<StudentAnswer> bobAnswers = new ArrayList<>();
        for (int i = 0; i < exam1Questions.size(); i++) {
            Question q = exam1Questions.get(i);
            // Bob misses index 1, 3, 7
            String selected = (i == 1 || i == 3 || i == 7) ? "A" : q.getCorrectAnswer();
            bobAnswers.add(new StudentAnswer(subBob, q, selected));
        }
        studentAnswerRepository.saveAll(bobAnswers);
        subBob.setStudentAnswers(bobAnswers);

        // Carol attempted Exam 2 -> score 8/10
        Submission subCarol = new Submission(student3, exam2, 8, LocalDateTime.now().minusHours(1), "COMPLETED");
        submissionRepository.save(subCarol);
        List<StudentAnswer> carolAnswers = new ArrayList<>();
        for (int i = 0; i < exam2Questions.size(); i++) {
            Question q = exam2Questions.get(i);
            // Carol misses index 2, 6
            String selected = (i == 2 || i == 6) ? "D" : q.getCorrectAnswer();
            carolAnswers.add(new StudentAnswer(subCarol, q, selected));
        }
        studentAnswerRepository.saveAll(carolAnswers);
        subCarol.setStudentAnswers(carolAnswers);

        System.out.println(">>> Sample dataset successfully initialized with 8 users, 3 subjects, 2 exams, 20 questions, and 3 submissions!");
    }
}
