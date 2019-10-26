package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

public class Data {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static boolean saveAsJson(Object object, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(object, writer);
            return true;
        } catch (IOException | JsonIOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static Object loadFromJson(Type objectType, String filename) {
        try (FileReader reader = new FileReader(filename)) {
            return gson.fromJson(reader, objectType);
        } catch (FileNotFoundException ex) {
            System.err.println("Warning: File " + filename + " not found.");
            return null;
        } catch (IOException | JsonIOException | JsonSyntaxException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
