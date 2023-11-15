import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * HowLong
 * LINEのトーク履歴のテキストファイルを読み込んで、その長さを計算するプログラム。
 * updated 2023-11-15
 * @author Tyomo
 */
public class HowLong {
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}/\\d{2}/\\d{2}\\(.\\)");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^\\d{2}:\\d{2}");
    private static final Pattern STAMP_PATTERN = Pattern.compile("^\\[スタンプ\\]");
    private static final Pattern PHOTO_PATTERN = Pattern.compile("^\\[写真\\]");

    private String[] lines;
    private Optional<Counters> counters;

    public HowLong(String[] lines){
        this.lines = lines;
        this.counters = Optional.empty();
    }

    public static HowLong fromFile(Path path) throws IOException {
        String log_text = Files.readString(path, Charset.forName("UTF-8"));
        return HowLong.fromText(log_text);
    }

    public static HowLong fromText(String text) {
        return new HowLong(text.split("\r\n|\n"));
    }

    public class Counters {
        public int counterNone;
        public int counterDate;
        public int counterChat;
        public int counterStamp;
        public int counterPhoto;
        public int counterComment;
        
        public Counters(int counterNone, int counterDate, int counterChat, int counterStamp, int counterPhoto, int counterComment){
            this.counterNone = counterNone;
            this.counterDate = counterDate;
            this.counterChat = counterChat;
            this.counterStamp = counterStamp;
            this.counterPhoto = counterPhoto;
            this.counterComment = counterComment;
        }

        @Override
        public String toString() {
            return String.format(
                "None: %d, Date: %d, Chat: %d, Stamp: %d, Photo: %d, Comment: %d",
                this.counterNone,
                this.counterDate,
                this.counterChat,
                this.counterStamp,
                this.counterPhoto,
                this.counterComment
            );
        }
    }

    public double calcMilliMeter() {
        //以下のfor内で使用する、一行をさらに\tを基準に細かく分けるための変数
        String[] commentInfo = new String[5];
        
        //長さを記録する変数
        double millimeterCount = 1.5;

        //counter 

        int counterNone = 0;
        int counterDate = 0;
        int counterChat = 0;
        int counterStamp = 0;
        int counterPhoto = 0;
        int counterComment = 0;
        //commentInfoに代入するための変数
        String[] splitted = new String[5];
        //this.linesに含まれる要素全てに対して実行
        for (int i = 0; i < this.lines.length; i++) {
            //stringに処理をする履歴の行を入れる
            String string = this.lines[i];
            //splittedにstringをタブで分割しえ入れる
            splitted = string.split("\t", 2+1); //注意: .split()の第二変数は最高使用回数+1の値を入力する。
            //splittedをstring_spltitedに代入
            for(int j = 0; j < string.split("\t", 2+1).length; j++){
                commentInfo[j] = splitted[j];
            }
            
            if(commentInfo[0] == ""){
                //_空行はノーカウント
                counterNone += 1;
            }else if(HowLong.DATE_PATTERN.matcher(commentInfo[0]).find()){
                //_日付表示
                millimeterCount += 7.0;
                //System.out.println("!!DATE");
                counterDate += 1;
            }else if (HowLong.COMMENT_PATTERN.matcher(commentInfo[0]).find()){
                //_発言
                if(HowLong.STAMP_PATTERN.matcher(commentInfo[2]).find()){
                    //_発言のうちスタンプのもの
                    millimeterCount += 30.0;
                    //System.out.println("!!STAMP");
                    counterStamp += 1;
                }else if(HowLong.PHOTO_PATTERN.matcher(commentInfo[2]).find()){
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

        this.counters = Optional.of(new Counters(counterNone, counterDate, counterChat, counterStamp, counterPhoto, counterComment));
        return millimeterCount;
    }

    public static void main(String[] args) {
        //ファイルパスの設定
        Path filePath = null;
        try{
            filePath = Paths.get(args[0]);
        }catch (ArrayIndexOutOfBoundsException e){
            System.out.println("履歴ファイルを指定してください。");
        }
        
        HowLong howLong = null;
        //ファイルのインポート, 失敗した場合はエラーを表示
        try{
            howLong = HowLong.fromFile(filePath);
        }catch(IOException e){
            System.err.println(e.getMessage());
            System.out.println("履歴ファイルの読み込みに失敗しました。");
        }

        //履歴を一行ごとに区切って配列にする
        double millimeterCount = howLong.calcMilliMeter();
        Counters counters = howLong.counters.orElseThrow();
        
        //出力
        System.out.println("履歴の長さは...");
        System.out.println(millimeterCount +"ミリメートル");
        System.out.println(millimeterCount/1000 + "メートル");
        System.out.println(millimeterCount/1000000 + "キロメートル");
        System.out.println(counters.toString());

    }
}