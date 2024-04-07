package honor.branchmerge.filtercommit.util;

import com.google.gson.Gson;
import honor.branchmerge.filtercommit.Config.TopConfig;

import java.io.FileReader;
import java.io.IOException;

public class FilterConfigReader {
    public static TopConfig read(String configPath) {
        Gson gson = new Gson();
        TopConfig config = null;
        try (FileReader reader = new FileReader(configPath)) {
            config = gson.fromJson(reader, TopConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }
}
