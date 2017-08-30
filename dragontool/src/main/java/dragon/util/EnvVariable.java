package dragon.util;

import java.io.*;
import java.util.*;

/**
 * <p>Getting windows environmental variable </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author http://www.rgagnon.com/javadetails/java-0150.html
 * @version 1.0
 */

public class EnvVariable {
    private static String DRAGONHOME=null;
    private static String CHARSET=null;

    public static Properties getEnv(){
        BufferedReader br;
        Process p;
        Properties envVars;
        Runtime r;
        String line, OS, key, value;
        int idx;

        try{
            r = Runtime.getRuntime();
            envVars = new Properties();
            OS = System.getProperty("os.name").toLowerCase();
            if (OS.indexOf("windows 9") > -1) {
                p = r.exec("command.com /c set");
            } else if ( (OS.indexOf("nt") > -1) || (OS.indexOf("windows") > -1)) {
                // thanks to JuanFran for the xp fix!
                p = r.exec("cmd.exe /c set");
            } else {
                // our last hope, we assume Unix (thanks to H. Ware for the fix)
                p = r.exec("env");
            }

            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ( (line = br.readLine()) != null) {
                idx = line.indexOf('=');
                key = line.substring(0, idx);
                value = line.substring(idx + 1);
                envVars.setProperty(key, value);
            }
            return envVars;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String getEnv(String key){
        Properties env;

        env=getEnv();
        if(env!=null)
            return env.getProperty(key);
        else
            return null;
    }

    public static void setDragonHome(String home){
        DRAGONHOME =home;
    }

    public static String getDragonHome(){
        if(DRAGONHOME!=null)
            return DRAGONHOME;
        else
            return getEnv("DRAGONTOOL");
    }

    public static void setCharSet(String charSet){
        CHARSET=charSet;
    }
    public static String getCharSet(){
        String charSet;

        if(CHARSET!=null)
            return CHARSET;
        charSet=getEnv("DRAGONCHARSET");
        if(charSet!=null && charSet.trim().length()==0)
            charSet=null;
        return charSet;
    }
}
