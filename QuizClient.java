import java.io.*;
import java.net.*;

public class QuizClient {
    public static void main(String[] args) {
		BufferedReader in = null; //서버로부터 데이터를 읽기 위한 입력 스트림
		BufferedReader stin = null; //사용자의 입력을 받기 위한 입력 스트림
		BufferedWriter out = null; //서버로 데이터를 보내기 위한 출력 스트림

		Socket socket = null; //서버와의 통신을 위한 소켓

		String ip = "localhost"; //기본 IP주소와 포트
		int port = 8888;

		try {
			try(BufferedReader configReader = new BufferedReader(new FileReader("server.dat"))){ //server.dat 파일에서 ip주소와 포트 받아옴
				ip = configReader.readLine();
				port = Integer.parseInt(configReader.readLine());
			}
		} catch (IOException e) {
			System.out.println("Configuration file not found, using default IP and ports"); //실패할 경우 기본 설정값으로 설정
		}

		try {
			socket = new Socket(ip, port);

			in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //데이터 통신 스트림 설정
			stin = new BufferedReader(new InputStreamReader(System.in));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			String serverMessage; //서버로 부터 받은 메시지 저장
			String userInput; //입력한 데이터 저장

            while((serverMessage = in.readLine()) != null){
				if(serverMessage.startsWith("QUESTION")){ //서버가 퀴즈 질문을 보낸 경우
					System.out.println("Server: " + serverMessage); //질문 출력
					System.out.print("Your answer: ");
					userInput = stin.readLine(); //사용자 입력 대기
					out.write("ANSWER " + userInput + "\n"); //답변 서버로 전송
					out.flush();
				}else if(serverMessage.startsWith("FEEDBACK")){ //서버가 답변 결과를 보낸 경우
					System.out.println(serverMessage); //정답 혹은 오답 출력
				}else if (serverMessage.startsWith("SCORE")) { //서버가 최종 점수를 보낸 경우
					System.out.println("Final Score: " + serverMessage.substring(6)); //점수 출력
					System.out.println("Thanks for playing!"); //종료 메시지
					break;
				}else{
					System.out.println("Server says: " + serverMessage); //기타 메시지
				}
            }
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				socket.close(); //소켓 연결 종료
			} catch (IOException e) {
				System.out.println("Disconnected");
			}
		}
	}
}
