/*
Simon Aronsky
01/20/2021
Program 1
Simple Crawl

This class is used to crawl through webpages, starting with the one provided, and hop to each new link the
specified number of times
 */

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

public class SimpleCrawl {
    static URL currentURL;
    static HttpURLConnection connection;
    static int numHops;
    static List<String> urlList = new ArrayList<String>();
    static String body;

    /*
    This method converts a full URL, into a simplified version, eliminating http(s):// and wwww.

    Precondition: Link- a string of the URL to simplified
    Postcondition: String- the simplified link
     */
    public static String simplifyURL(String link) {
        int index = link.indexOf("://");
        if (index != -1) {
            int index2 = link.indexOf("www.");
            if (index2 != -1)
                index = index2 + 4;
            else
                index = index + 3;
        } else
            index = 0;

        return link.substring(index);
    }

    /*
    This method is used to handle the response codes returned by the server, continuing if receiving a 200, or
    handling the code appropriately

    Precondition: Link- a string of the URL to requested
    Postcondition: boolean- the status of the URL
     */
    public static boolean manageResponseCode(String link) throws IOException {
        try {
            if (!link.substring(link.length() - 1).equals("/"))
                link = link + "/";
            URL nextURL = new URL(link);
            HttpURLConnection nextConnection = (HttpURLConnection) nextURL.openConnection();
            int responseCode = nextConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                if (responseCode / 100 == 5) {
                    for (int i = 0; i < 3 && responseCode / 100 == 5; i++) {
                        nextConnection = (HttpURLConnection) nextURL.openConnection();
                        responseCode = nextConnection.getResponseCode();
                    }
                    return manageResponseCode(link);
                } else if (responseCode / 100 == 4)
                    return false;
                else if (responseCode == 300)
                    return false;
                else if (responseCode / 100 == 3) {
                    System.out.print(nextURL.toString() + " Redirected to: ");
                    String URI = nextConnection.getHeaderField("Location");
                    return manageResponseCode(URI);
                }
            }
            currentURL = nextURL;
            connection = nextConnection;
            String simplifiedLink = simplifyURL(link);
            if (urlList.contains(simplifiedLink))
                return false;
            urlList.add(simplifiedLink);
            System.out.println(link);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*
    This method is used to return the body of an HTTP request

    Precondition: connection- a HttpURLConnection of the HTTP request
    Postcondition: String- the body of the response
     */
    public static void getBody(HttpURLConnection connection) throws IOException {
        try {
            InputStream bodyStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(bodyStream));
            StringBuilder bodyBuilder = new StringBuilder();
            String responseSingle = null;
            while ((responseSingle = reader.readLine()) != null)
                bodyBuilder.append(responseSingle);
            body = bodyBuilder.toString();
        } catch (Exception e) {
            getBody(connection);
        }
    }

    /*
    This method is used to return the next link in a HTTP body

    Precondition: body- a string of the body of the HTTP response
    Postcondition: boolean- usability of any of the links in the response
     */
    public static boolean getNextLink(String body) throws IOException {
        String patternString = "<a href=\"[^ ]*\"";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(body);
        while (matcher.find()) {
            String regex = body.substring(matcher.start(), matcher.end());
            String link = regex.substring(9, regex.length() - 1);
            if (manageResponseCode(link))
                return true;
        }
        System.out.println("No More");
        return false;
    }

    /*
    This method is used to recursively perform all the hops
     */
    public static void manageHops() throws IOException {
        if (numHops > 0) {
            System.out.print(numHops + ": ");
            connection = (HttpURLConnection) currentURL.openConnection();
            getBody(connection);
            if (getNextLink(body)) {
                numHops--;
                manageHops();
            }
        } else
            System.out.println(body);
    }


    public static void main(String[] input) throws IOException {
        if (input.length != 2) {
            System.out.println("Improper Arguments");
            return;
        }
        numHops = Integer.valueOf(input[1]);
        String startURL = input[0];
        try {
            currentURL = new URL(startURL);
        } catch (Exception e) {
            if (e instanceof java.net.MalformedURLException)
                System.out.println("Bad URL");
            else
                System.out.println("Exception Caught: " + e);
        }
        System.out.println(numHops + ": " + startURL);
        numHops--;
        manageHops();
    }
}