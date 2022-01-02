
import java.io.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;

class StatisticsFile {

    File file;
    ArrayList<Person> personList;

    public StatisticsFile() {
        File f = new File("Statistics");
        if (!(f.exists() && f.isDirectory()))
            new File("Statistics").mkdir();
        this.file = f;
        personList = new ArrayList<>();
    }

    public int[] getStatsFromFile(Quiz quiz) throws IOException, ParseException {
        File f = new File(file.getName()+"\\"+quiz.quizName+".txt");
        String temp;
        if(!f.exists()) f.createNewFile();
        int[] lines = new int[quiz.getLinesCount()];

        BufferedReader reader = new BufferedReader(new FileReader(f.getCanonicalPath()));

        for(int i = 0; i < quiz.getLinesCount(); i++) {
           if((temp = reader.readLine()) == null)
               return lines;
            lines[i] = Integer.parseInt(temp);
        }

        while ((temp = reader.readLine()) != null) {
            String[] subStr = temp.split("_");
            DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            Date date = format.parse(subStr[1]);
            this.personList.add(new Person(subStr[0], date ));
        }

        return lines;
    }

    public void writeStatsToFile(int[] lines, Quiz quiz) throws IOException {

        try {
            FileWriter fstream = new FileWriter(this.file.getName()+"\\"+quiz.quizName+".txt");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("");
            out.close();
        } catch (Exception e)
        { System.err.println("Error in file cleaning: " + e.getMessage()); }


        FileWriter writer = new FileWriter(this.file.getName()+"\\"+quiz.quizName+".txt", true);
        for (Integer line : lines) {
            writer.write(String.valueOf(line));
            writer.append('\n');
        }
        for (Person person : this.personList) {
            writer.write(person.toString());
            writer.append('\n');
        }
        writer.flush();

    }


    public void statOutput (Quiz quiz) throws IOException, ParseException {
        File f = new File(file.getName()+"\\"+quiz.quizName+".txt");
        String temp;
        int counter;
        double percent;
        if(!f.exists()) {
            System.out.println("No stats available");
            System.out.println("__________________");
            return;
        }
        BufferedReader reader = new BufferedReader(new FileReader(f.getCanonicalPath()));
        System.out.println("Quiz was completed " + reader.readLine() + " times.");
        for(int i = 0; i < quiz.questions.size(); i++) {
            percent = 0; counter = 0;
            System.out.println( (i + 1) + " question:");
            for(int j = 0; j < quiz.questions.get(i).answers.size(); j++) {
                temp = reader.readLine();
                counter += Integer.parseInt(temp);
                if(quiz.questions.get(i).checkAnswer(j + 1)) percent += Integer.parseInt(temp);
                System.out.println( (j + 1) + ") answer choosen " + temp + " times.");
            }
            percent /= counter; percent *= 100;
            System.out.println("Answered correctly: "+((double) Math.round(percent * 100) / 100)+"%");
        }
        System.out.println("Was completed by:");
        while ((temp = reader.readLine()) != null) {
            String[] subStr = temp.split("_");
            DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            Date date = format.parse(subStr[1]);
            System.out.println(subStr[0]+", " + date);
        }
        System.out.println("__________________");
    }

}

class Person {
    String name;
    Date startTime;

    public Person(String name, Date startTime) {
        this.name = name;
        this.startTime = startTime;
    }


    public boolean checkPerson(String fileName) throws IOException, ClassNotFoundException {
        InputStream inputStream = new FileInputStream(fileName);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        Person person = (Person) objectInputStream.readObject();
        inputStream.close();
        return !Objects.equals(person.name, this.name);
    }


    @Override
    public String toString() {
        return name + "_" + startTime;
    }
}

class Quiz implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    String quizName;
    ArrayList<Question> questions = new ArrayList<>();

    public void addQuestions(ArrayList<Question> questions) {
        this.questions = questions;
    }

    public void seeQuestions() {
        System.out.println(questions);
    }

    public void saveToFile(String filename) throws IOException {
        OutputStream fileOutputStream = new FileOutputStream(filename, true);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(this);
        fileOutputStream.close();
        System.out.println("Saved.");

    }


    public ArrayList<String> getQuizFileNameList(String directory) {
        ArrayList<String> textFiles = new ArrayList<>();
        File dir = new File(directory);
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().endsWith((".txt"))) {
                textFiles.add(file.getName());
            }
        }
        return textFiles;
    }

    public Quiz getQuizFromFile(String fileName) throws IOException, ClassNotFoundException {
        InputStream inputStream = new FileInputStream(fileName);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        Quiz quiz = (Quiz)objectInputStream.readObject();
        inputStream.close();
        return quiz;
    }

    public void getFullQuestion(int i) {
        System.out.println(questions.get(i));
        questions.get(i).getAnswers();
    }

    public int getLinesCount() {
        int res = 1;
        for (Question question : questions) res += question.answers.size();
        return res;
    }
}



class Question implements Serializable {
    private static final long serialVersionUID = 6529685098267757690L;
    String content;
    ArrayList<String> answers;
    int correctAnsw;

    public Question(String content, ArrayList<String> answers, int correctAnsw) {
        this.content = content;
        this.answers = answers;
        this.correctAnsw = correctAnsw;
    }

    public void getAnswers() {
        for(int i = 0; i < answers.size(); i++)
            System.out.println((i+1)+") "+answers.get(i).toString());
    }

    public boolean checkAnswer(int answ) {
        return answ == correctAnsw;
    }

    @Override
    public String toString() {
        return content;
    }
}



class MainMenu {
    String dir = System.getProperty("user.dir").toString();
    StatisticsFile statisticsFile = new StatisticsFile();
    ArrayList<Quiz> quizList = getExistedQuiz(dir);

    MainMenu() throws IOException, ClassNotFoundException {
    }


    public void sysCls() {
        for(int i = 0; i < 30; i++)
            System.out.println();
    }

    public void menuOutput() throws IOException, ClassNotFoundException, ParseException {
        Quiz quiz = new Quiz();
        Scanner scanner = new Scanner(System.in);
        System.out.println("1. Create new Quiz");
        System.out.println("2. Complete Quiz");
        System.out.println("3. Statistics");
        System.out.println("4. Exit");
        String input = scanner.nextLine();
        sysCls();
        switch(input) {
            case "1":
                System.out.println("Enter quiz's name:");
                quiz.quizName = scanner.nextLine();
                ArrayList<Question> questions = new ArrayList<>();
                System.out.println("Enter questions quantity:");
                int quantity = 0;
                try {
                    quantity = scanner.nextInt();
                }
                catch(Exception ex) {
                    System.out.println("Incorrect input format");
                    System.out.println("_________________");
                    menuOutput();
                }
                sysCls();
                for(int i = 0; i < quantity; i++) {

                    System.out.println((i+1)+" question:");
                    scanner.nextLine();
                    String content = scanner.nextLine();

                    System.out.println("Enter answers quantity for the "+(i+1+" question:"));
                    int answQuantity = scanner.nextInt();

                    System.out.println("correct answer will be under the number:");
                    int correctAnsw = 0;
                    try {
                        correctAnsw = scanner.nextInt();
                    }
                    catch(Exception ex) {
                        System.out.println("Incorrect input format");
                        System.out.println("_________________");
                        menuOutput();
                    }

                    ArrayList<String> answers = new ArrayList<>();
                    // scanner.nextLine();
                    for (int j = 0; j < answQuantity; j++) {
                        System.out.println((j+1)+" answer for the "+(i+1)+" question:");
                        String answer = scanner.next();
                        answers.add(answer);
                    }
                    questions.add(new Question(content, answers, correctAnsw));
                }
                quiz.addQuestions(questions);
                quizList.add(quiz);
                quiz.saveToFile(quiz.quizName+".txt");
                sysCls();
                menuOutput();
                break;


            case "2":

                System.out.println("Enter your name: ");
                String name = scanner.nextLine();
                sysCls();
                Person person = new Person(name, new Date());
                statisticsFile.personList.add(person);

                ArrayList<String> quizFileNameList = quiz.getQuizFileNameList(dir);
                for(int i = 0; i < quizFileNameList.size(); i++)
                    System.out.println((i+1)+") "+quizFileNameList.get(i).toString().substring(0, quizFileNameList.get(i).toString().indexOf('.')));
                System.out.println("Enter quiz number:");
                int quizNumber = 0;
                try {
                    quizNumber = scanner.nextInt();
                }
                catch(Exception ex) {
                    System.out.println("Incorrect input format");
                    System.out.println("_________________");
                }
                quiz = quiz.getQuizFromFile(quizFileNameList.get(quizNumber - 1).toString());
                int[] lines = statisticsFile.getStatsFromFile(quiz);
                lines[0]++;
                int index = 1;
                for(int i = 0; i < quiz.questions.size(); i++) {
                    quiz.getFullQuestion(i);
                    System.out.println("The correct answer under number: ");
                    int answer = 0;
                    try {
                        answer = scanner.nextInt();
                    }
                    catch(Exception ex) {
                        System.out.println("Incorrect input format");
                        System.out.println("_________________");
                    }

                    lines[index + answer - 1]++;
                    if(quiz.questions.get(i).checkAnswer(answer))
                        System.out.println("Correct!");
                    else
                        System.out.println("Wrong :(");


                    index += quiz.questions.get(i).answers.size();
                }
                statisticsFile.writeStatsToFile(lines, quiz);
                System.out.println("_________________");
                menuOutput();
                break;


            case "3":

                ArrayList<String> quizFileNamelist = quiz.getQuizFileNameList(dir);
                for(int i = 0; i < quizFileNamelist.size(); i++)
                    System.out.println((i+1)+") "+quizFileNamelist.get(i).toString().substring(0, quizFileNamelist.get(i).toString().indexOf('.')));
                System.out.println("Enter quiz number:");
                int quiznumber = scanner.nextInt();
                sysCls();
                quiz = quiz.getQuizFromFile(quizFileNamelist.get(quiznumber - 1).toString());
                statisticsFile.statOutput(quiz);
                menuOutput();
                break;

            case "4":
                scanner.close();
                System.exit(0);

            default:

                sysCls();
                menuOutput();
                break;
        }
//        quiz.seeQuestions();
    }

    private ArrayList<Quiz> getExistedQuiz(String directory) throws IOException, ClassNotFoundException {
        ArrayList<Quiz> ExistedQuiz = new ArrayList<>();
        File dir = new File(directory);

        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().endsWith((".txt")))
                ExistedQuiz.add((new Quiz()).getQuizFromFile(file.getName()));
        }
        return ExistedQuiz;
    }

}

public class MainClass {
    public static void main(String[] args) throws IOException, ClassNotFoundException, ParseException {
        MainMenu menu = new MainMenu();
        menu.menuOutput();
    }
}

