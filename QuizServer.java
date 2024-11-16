import java.io.*;
import java.net.*;

public class QuizServer {
    public static void main(String[] args) {
        ServerSocket listener = null; //클라이언트 연결을 기다리는 서버 소켓

        //질문과 정답 배열
        String[] questionArray = { 
            "1 + 1 = ?", 
            "Who is the president of the USA in 2025?", 
            "Who won the World Series in 2024?"
        }; 

        String[] answerArray = {
            "2",
            "Donald Trump",
            "LA Dodgers"
        };

        try {
            listener = new ServerSocket(8888); //서버소켓을 생성하여 8888번 포트에서 연결 대기

            System.out.println("Start Server...");
            System.out.println("Waiting for clients");
			//무한 루프: 서버는 계속해서 클라이언트 연결 수락
            while (true) {
                Socket socket = listener.accept(); //클라이언트 연결 수락
                System.out.println("A new client connected!"); //클라이언트 연결 로그
                new Thread(new ClientHandler(socket, questionArray, answerArray)).start(); //새로운 thread 생성하여 클라이언트 처리
            }

        } catch (IOException e) { 
            System.out.println("Server error: " + e.getMessage());
        } finally {
            try {
				//서버 소켓 닫기
                if (listener != null) {
                    listener.close(); 
                }
            } catch (IOException e) {
                System.out.println("Failed to close server socket.");
            }
        }
    }
}

//클라이언트 처리 클래스
class ClientHandler implements Runnable {
    private Socket socket; //클라이언트 소켓
    private String[] questionArray; //질문 배열
    private String[] answerArray; //정답 배열
	//생성자: 질문과 정답 배열 초기화
    public ClientHandler(Socket socket, String[] questionArray, String[] answerArray) {
        this.socket = socket;
        this.questionArray = questionArray;
        this.answerArray = answerArray;
    }

    @Override
    public void run() {
		//stream 선언
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
			//연결 성공 메시지
            out.write("CONNECTED Welcome to the quiz game!\n");
            out.flush();

            int score = 0; //점수 초기화
            for (int i = 0; i < questionArray.length; i++) {
                out.write("QUESTION " + (i + 1) + ": " + questionArray[i] + "\n"); //질문 전송
                out.flush();

                String clientAnswer = in.readLine();
                if (clientAnswer != null && clientAnswer.startsWith("ANSWER")) {//클라이언트가 보낸 메시지가 "ANSWER"로 시작하는지 확인
                    String answer = clientAnswer.substring(7).trim(); //답변 추출
                    if (answer.equalsIgnoreCase(answerArray[i])) { //정답 확인
                        score++; //점수 증가
                        out.write("FEEDBACK Correct!\n"); //정답 메시지 전송
                    } else { //오답일 경우
                        out.write("FEEDBACK Incorrect\n"); //오답 메시지 전송
                    }
                } else { //올바르지 않은 형식의 메시지일 경우
                    out.write("ERROR INVALID FORMAT\n"); //에러 메시지 전송
                }
                out.flush();
            }
            out.write("SCORE " + score + "/" + questionArray.length + "\n"); //최종 점수 전송
            out.flush();
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        } finally {
            try {//클라이언트 소켓 닫기
                socket.close(); 
				System.out.println("Client connection closed");
            } catch (IOException e) {
                System.out.println("Failed to close client socket.");
            }
        }
    }
}