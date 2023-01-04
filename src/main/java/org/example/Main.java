package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Main {

    static String projectName="";

    public static void main(String[] args)
            throws IOException {
//        Clone("https://github.com/gemini-akashGarg/testProject", "beta");
        Clone("https://github.com/gemini-akashGarg/Gemjar_Eco_Regression", "master", "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");
        runbashCommand();
//        uploadToS3("akashgarg", "40e82a05-7109-4341-870c-07046531e1441669020150562");
    }

    public static void Clone(String gitLink, String branch) {
        Clone(gitLink, branch, null);
    }

    public static void Clone(String gitLink, String branch, String pwd) {
        //         Local directory on this machine where we will clone remote repo.
        if (branch == null) {
            branch = "master";
        }
        File localRepoDir = new File(System.getProperty("user.dir") + "/myapp");

        // Monitor to get git command progress printed on java System.out console
        TextProgressMonitor consoleProgressMonitor = new TextProgressMonitor(new PrintWriter(System.out));
        String GitServer = null;
        String username = null;
        String repoName = null;
        Pattern p = Pattern.compile("https://(.+)/(.+)/(.+)");
        Matcher m = p.matcher(gitLink);
        if (m.find()) {
            GitServer = m.group(1);
            username = m.group(2);
            repoName = m.group(3);
            projectName = repoName;
        } else {
            System.out.println("some error occur while cloning");
        }
        System.out.println("gitServer => " + GitServer);
        System.out.println("gitUserName => " + username);
        System.out.println("gitRepoName => " + repoName);

        System.out.println("\n>>> Cloning repository\n");

        try {
            if (pwd != null) {
                Repository repo = Git.cloneRepository()
                        .setURI(gitLink).setBranch(branch)
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, pwd))
                        .setDirectory(localRepoDir).setProgressMonitor(consoleProgressMonitor)
                        .call().getRepository();
            } else {
                Repository repo = Git.cloneRepository()
                        .setURI(gitLink).setBranch(branch)
                        .setDirectory(localRepoDir).setProgressMonitor(consoleProgressMonitor)
                        .call().getRepository();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Some error occur while cloning Repo");
        }
        System.out.println("\n>>> Cloning repository Done\n");
    }

    public static void runbashCommand() throws IOException {
        // Set the command to be run
        String[] command = {"bash", "-c", "cd myapp && pwd"};
        int exitCode = -1;
        // Create a ProcessBuilder for the command
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        // Start the process
        Process process = processBuilder.start();
        try {
            // Wait for the process to finish
            process.waitFor();
            exitCode = process.exitValue();
            try (InputStreamReader isr = new InputStreamReader(process.getInputStream())) {
                int c;
                while ((c = isr.read()) >= 0) {
                    System.out.print((char) c);
                    System.out.flush();
                }
            }
            // Print the exit code
            System.out.println("Exit code: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Some error occur while running cd command");
        }
        System.out.println("cd done");
        /////////////////////////////

        command = new String[]{"bash", "-c", "mvn clean -f myapp/pom.xml"};


        // Create a ProcessBuilder for the command
        processBuilder = new ProcessBuilder(command);
        try {
            // Start the process
            process = processBuilder.start();
            // Wait for the process to finish
            process.waitFor();
            // Get the exit code of the process
            exitCode = process.exitValue();
            try (InputStreamReader isr = new InputStreamReader(process.getInputStream())) {
                int c;
                while ((c = isr.read()) >= 0) {
                    System.out.print((char) c);
                    System.out.flush();
                }
            }
            // Print the exit code
            System.out.println("Exit code: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Some error occur while running mvn clean command");
        }
        System.out.println("mvn clean done");
        /////////////////////////////////

        command = new String[]{"bash", "-c", "mvn -T 4 install -f myapp/pom.xml"};
        // Create a ProcessBuilder for the command
        processBuilder = new ProcessBuilder(command);
        try {
            // Start the process
            process = processBuilder.start();
            // Wait for the process to finish
            process.waitFor();
            // Get the exit code of the process
            try (InputStreamReader isr = new InputStreamReader(process.getInputStream())) {
                int c;
                while ((c = isr.read()) >= 0) {
                    System.out.print((char) c);
                    System.out.flush();
                }
            }
            exitCode = process.exitValue();
            // Print the exit code
            System.out.println("Exit code: " + exitCode);
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("some error occur while running mvn package");
        }
        System.out.println("jar creation Done");
    }

    public static void uploadToS3(String username, String token) throws IOException {
        try {
            String filePath = System.getProperty("user.dir") + "/myapp/target/"+projectName+"-1.0-SNAPSHOT-jar-with-dependencies.jar";
            System.out.println(filePath);

            String u = "https://apis-beta.gemecosystem.com/v1/upload/file";
            CloseableHttpClient httpclient = HttpClients.createDefault();
            MultipartEntityBuilder entitybuilder = MultipartEntityBuilder.create();
            entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entitybuilder.addBinaryBody("file", new File(filePath));
            HttpEntity mutiPartHttpEntity = entitybuilder.build();
            RequestBuilder reqbuilder = RequestBuilder.post().setUri(u);
            reqbuilder.setEntity(mutiPartHttpEntity);
            HttpUriRequest multipartRequest = reqbuilder.build();
            multipartRequest.setHeader(new BasicHeader("username", username));
            multipartRequest.setHeader(new BasicHeader("bridgeToken", token));
            HttpResponse httpresponse = httpclient.execute(multipartRequest);
            JsonObject js = (JsonObject) JsonParser.parseString(EntityUtils.toString(httpresponse.getEntity()));
            System.out.println(js.get("data").getAsJsonArray().get(0).getAsJsonObject().get("Url").getAsString());
            System.out.println(httpresponse.getStatusLine().getStatusCode());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Some error occur while uploading jar");
        }


    }

}