import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ChatFilter {
    public String badWordsFileName;

    public ChatFilter(String badWordsFileName) {
        this.badWordsFileName = badWordsFileName;
    }

    public String filter(String msg)
    {
        ArrayList<String> badWords = new ArrayList<>();
        File f;
        FileReader fr;
        BufferedReader br;

        try {
            f = new File(this.badWordsFileName);
            fr = new FileReader(f);
            br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null)
            {
                badWords.add(line);
            }

            fr.close();
            br.close();
        } catch (Exception e)
        {
            e.printStackTrace();
            //System.out.println("Error: Censored words file not found.");
        }
        for (int i = 0; i < badWords.size(); i++)
        {
            String censor = "";
            for (int j = 0; j < badWords.get(i).length(); j++)
            {
                censor += "*";
            }
            msg = msg.replaceAll("(?i)" + badWords.get(i), censor);
            censor = "";
        }
        return msg;
    }
}
