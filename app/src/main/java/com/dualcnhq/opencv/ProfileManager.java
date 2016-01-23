package com.dualcnhq.opencv;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class ProfileManager {
    private List<Profile> profileList;
    private String path;

    public ProfileManager(String path) {
        this.profileList = new ArrayList<>();
        this.path = path;
    }

    public void addProfile(String name, int id, String twitterID) {
        profileList.add(new Profile(id, name, twitterID));
    }

    public Profile getProfileById(int id) {
        Iterator<Profile> iterator = profileList.iterator();
        while (iterator.hasNext()) {
            Profile profile = iterator.next();
            if (profile.getId() == id) {
                return profile;
            }
        }

        return null;
    }

    public Profile getProfileByName(String name) {
        Iterator<Profile> iterator = profileList.iterator();
        while (iterator.hasNext()) {
            Profile profile = iterator.next();
            if (profile.getName().equals(name)) {
                return profile;
            }
        }

        return null;
    }

    public void processSavingProfileList() {
        try {
            File file = new File(path + "faces.txt");
            file.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            Iterator<Profile> iterator= profileList.iterator();
            while (iterator.hasNext()) {
                Profile profile= iterator.next();
                bw.write(profile.getName() + "," + profile.getId() + "," + profile.getTwitterID());
                bw.newLine();
            }

            bw.close();
        } catch (IOException e) {
            Log.e("error", e.getMessage() + " " + e.getCause());
        }
    }

    public void readProfileIntoProfileList() {
        try {

            FileInputStream fileInputStream = new FileInputStream(path + "faces.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));

            String strLine;
            while ((strLine = br.readLine()) != null) {
                StringTokenizer tokens = new StringTokenizer(strLine, ",");
                String label = tokens.nextToken();
                String num = tokens.nextToken();
                String twitterId = tokens.nextToken();

                profileList.add(new Profile(Integer.parseInt(num), label, twitterId));
            }

            br.close();
            fileInputStream.close();
        } catch (IOException e) {

        }
    }

    public int getMaxId() {
        int maxID = 0;
        Iterator<Profile> iterator = profileList.iterator();
        while (iterator.hasNext()) {
            Profile profile = iterator.next();
            if (profile.getId() > maxID) {
                maxID = profile.getId();
            }
        }

        return maxID;
    }
}
