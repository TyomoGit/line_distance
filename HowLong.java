import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class HowLong {
    public static void main(String[] args) {
        //ファイルパスの設定
        Path filePath = Paths.get("LINE.txt");
        try{
            filePath = Paths.get(args[0]);
        }catch (ArrayIndexOutOfBoundsException e){
            System.out.println("履歴ファイルを指定してください。");
        }
        
        //Path filePath = Paths.get("LINE.txt");
        String log_text = "";
        //ファイルのインポート, 失敗した場合はエラーを表示
        try{
            log_text = Files.readString(filePath, Charset.forName("UTF-8"));
        }catch(IOException e){
            System.err.println(e.getMessage());
            System.out.println("履歴ファイルお読み込みに失敗しました。");
        }

        //履歴を一行ごとに区切って配列にする
        String[] log_text_splited = log_text.split("\r\n|\n");
        //以下のfor内で使用する、一行をさらに\tを基準に細かく分けるための変数
        String[] string_splited = new String[5];
        //string_splited[2] = "";
        
        //長さを記録する変数
        double millimeterCount = 1.5;

        //counter 

        //for debug
        int counterNone = 0;
        int counterDate = 0;
        int counterChat = 0;
        int counterStamp = 0;
        int counterPhoto = 0;
        int counterComment = 0;
        //string_splitedに代入するための変数
        String[] splited = new String[5];
        //log_text_splitedに含まれる要素全てに対して実行
        for (int i = 0; i < log_text_splited.length; i++) {
            //stringに処理をする履歴の行を入れる
            String string = log_text_splited[i];
            //splitedにstringをタブで分割しえ入れる
            splited = string.split("\t", 2+1); //注意: .split()の第二変数は最高使用回数+1の値を入力する。
            //splitedをstring_splitedに代入
            for(int j = 0; j < string.split("\t", 2+1).length; j++){
                string_splited[j] = splited[j];
            }
            
            if(string_splited[0] == ""){
                //_空行はノーカウント
                counterNone += 1;
            }else if(Pattern.compile("^\\d\\d\\d\\d/\\d\\d/\\d\\d\\(.\\)").matcher(string_splited[0]).find()){
                //_日付表示
                millimeterCount += 7.0;
                //System.out.println("!!DATE");
                counterDate += 1;
            }else if (Pattern.compile("^\\d\\d:\\d\\d").matcher(string_splited[0]).find()){
                //_発言
                if(Pattern.compile("^\\[スタンプ\\]").matcher(string_splited[2]).find()){
                    //_発言のうちスタンプのもの
                    millimeterCount += 30.0;
                    //System.out.println("!!STAMP");
                    counterStamp += 1;
                }else if(Pattern.compile("^\\[写真\\]").matcher(string_splited[2]).find()){
                    //_発言のうち写真のもの
                    millimeterCount += 30.0;
                    //System.out.println("!!PHOTO");
                    counterPhoto += 1;
                }else{
                    //_スタンプと写真以外の発言
                    millimeterCount += (2.0 + 1.5); //吹き出しの余白　上＋下
                    //System.out.println("!!CHAT" + i);
                    counterChat += 1;
                }
                
            }else{
                //_複数行の発言の一番上以外の部分
                millimeterCount += 2.5; //一文字
                //System.out.println("!!COMMENT");
                counterComment += 1;
            }
            //発言間の余白
            millimeterCount += 2.0; //発言間の余白
            
        }
        //出力
        System.out.println("履歴の長さは...");
        System.out.println(millimeterCount +"ミリメートル");
        System.out.println(millimeterCount/1000 + "メートル");
        System.out.println(millimeterCount/1000000 + "キロメートル");
        System.out.println("None, Date, Chat, Stamp, Photo, Comment: " + counterNone + ", " + counterDate + ", " + counterChat + ", " + counterStamp + ", "+ counterPhoto + ", " + counterComment);
        

    }
}