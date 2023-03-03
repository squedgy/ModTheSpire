package com.evacipated.cardcrawl.modthespire;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;
import org.slf4j.*;

import java.io.*;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.*;

public class ModInfo implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(ModInfo.class);
    /**
     * 
     */
    private static final long serialVersionUID = 7452562412479584982L;
    public transient URL jarURL;
    public transient String statusMsg = " ";
    public transient boolean isWorkshop = false;
    @SerializedName("modid")
    public String ID;
    @SerializedName("name")
    public String Name;
    @SerializedName("version")
    public Semver ModVersion;
    @SerializedName("author_list")
    public String[] Authors;
    @SerializedName("credits")
    public String Credits;
    @SerializedName("description")
    public String Description;
    @SerializedName("mts_version")
    public Semver MTS_Version;
    @SerializedName("sts_version")
    public String STS_Version;
    @SerializedName("dependencies")
    public String[] Dependencies;
    @SerializedName("optional_dependencies")
    public String[] OptionalDependencies;
    @SerializedName("update_json")
    public String UpdateJSON;

    private ModInfo()
    {
        Name = "";
        Authors = new String[]{};
        Description = "";
        MTS_Version = ModInfo.safeVersion("0.0.0");
        STS_Version = null;
        Dependencies = new String[]{};
        OptionalDependencies = new String[]{};
        UpdateJSON = null;
    }

    public String getIDName() {
        if (ID == null || ID.isEmpty()) {
            return Name;
        } else {
            return ID;
        }
    }
    
    private static void closeLoader(URLClassLoader loader)
    {
        try {
            if (loader != null) {
                loader.close();
            }
        } catch (Exception e) {
            System.out.println("Exception during loader.close(), URLClassLoader may be leaked. " + e.toString());
        }
    }

    public static ModInfo ReadModInfo(File mod_jar)
    {
        Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT)
            .registerTypeAdapter(Semver.class, new VersionDeserializer())
            .setDateFormat("MM-dd-yyyy")
            .create();
        try (JarFile file = new JarFile(mod_jar)) {
            Enumeration<JarEntry> entries = file.entries();
            Set<JarEntry> potentialJsons = new HashSet<>();

            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if(entry.getName().endsWith("ModTheSpire.json")) {
                    potentialJsons.add(entry);
                }
            }

            JarEntry target = null;
            if(potentialJsons.size() == 0) {
                LOG.info("Failed to locate ANY ModTheSpire.json in {}", mod_jar.getName());
                return null;
            }

            if(potentialJsons.size() > 1) {
                throw new RuntimeException("Politely tell the owner of {} to only include 1 ModTheSpire.json in their jars. Alternatively politely request ModTheSpire to handle the case of multiple jsons.");
            }

            target = potentialJsons.iterator().next();

            InputStream stream = file.getInputStream(target);
            try(
                InputStream entryStream = file.getInputStream(target);
                InputStreamReader reader = new InputStreamReader(entryStream, StandardCharsets.UTF_8)) {
                ModInfo info = gson.fromJson(reader, ModInfo.class);
                info.jarURL = mod_jar.toURI().toURL();
                return info;
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        // URLClassLoader loader = null;
        // try {
        //     JarFile file = new JarFile(mod_jar);
        //     loader = new URLClassLoader(new URL[] {mod_jar.toURI().toURL()}, null);
        //     InputStream in = loader.getResourceAsStream("/ModTheSpire.json");
        //     if (in == null) {
        //         // Fallback to old info file
        //         ModInfo info = ReadModInfoOld(mod_jar);
        //         info.jarURL = mod_jar.toURI().toURL();
        //         return info;
        //     }
        //     ModInfo info = gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), ModInfo.class);
        //     info.jarURL = mod_jar.toURI().toURL();
        //     in.close();
        //     return info;
        // } catch (Exception e) {
        //     System.out.println(mod_jar);
        //     e.printStackTrace();
        // } finally {
        //     if (loader != null) {
        //         closeLoader(loader);
        //     }
        // }
        //
        // return null;
    }

    private static ModInfo ReadModInfoOld(File mod_jar)
    {
        ModInfo info = new ModInfo();
        // Default mod name to jar name
        info.Name = mod_jar.getName();
        info.Name = info.Name.substring(0, info.Name.length() - 4);

        URLClassLoader loader = null;
        try {
            loader = new URLClassLoader(new URL[] {mod_jar.toURI().toURL()});
            // Read ModTheSpire.config
            Properties prop = new Properties();
            InputStream inProp = loader.getResourceAsStream("ModTheSpire.config");
            if (inProp != null) {
                prop.load(new InputStreamReader(inProp, StandardCharsets.UTF_8));
                info.Name = prop.getProperty("name");
                String author = prop.getProperty("author");
                if (author != null && !author.isEmpty()) {
                    info.Authors = author.split(",");
                }
                info.MTS_Version = ModInfo.safeVersion(prop.getProperty("mts_version", "0.0.0"));
                info.Description = prop.getProperty("description");

                info.STS_Version = prop.getProperty("sts_version");
                inProp.close();
            }
        } catch (Exception e) {
            System.out.println("ERROR: Failed to read Mod info from " + mod_jar.getName());
        } finally {
            closeLoader(loader);
        }
        return info;
    }

    static class VersionDeserializer implements JsonDeserializer<Semver>
    {
        @Override
        public Semver deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException
        {
            try {
                return safeVersion(jsonElement.getAsJsonPrimitive().getAsString());
            } catch (SemverException e) {
                return null;
            }
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ModInfo)) {
            return false;
        }

        ModInfo info = (ModInfo) obj;
        if (ID == null && info.ID == null) {
            return Objects.equals(Name, info.Name);
        } else {
            return Objects.equals(ID, info.ID);
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(ID, Name);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        out.writeObject(ID);
        out.writeObject(Name);
        out.writeObject(ModVersion.toString());
        out.writeObject(Authors);
        out.writeObject(Credits);
        out.writeObject(Description);
        out.writeObject(MTS_Version.toString());
        out.writeObject(STS_Version);
        out.writeObject(Dependencies);
        out.writeObject(OptionalDependencies);
        out.writeObject(UpdateJSON);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        ID = (String) in.readObject();
        Name = (String) in.readObject();
        ModVersion = safeVersion((String) in.readObject());
        Authors = (String[]) in.readObject();
        Credits = (String) in.readObject();
        Description = (String) in.readObject();
        MTS_Version = safeVersion((String) in.readObject());
        STS_Version = (String) in.readObject();
        Dependencies = (String[]) in.readObject();
        OptionalDependencies = (String[]) in.readObject();
        UpdateJSON = (String) in.readObject();
    }

    public static Semver safeVersion(String verString)
    {
        return new Semver(verString, Semver.SemverType.NPM);
    }
}
