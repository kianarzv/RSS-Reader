import java.io.*;
import java.util.Scanner;
import org.jsoup.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.*;


public class Main {
    public static void main(String[] args) {
        Home();
    }
    public static void Home() {
        try {
            System.out.println("type a valid for your desire action: ");
            Scanner scanner = new Scanner(System.in);
            System.out.println("[1] Show updates");
            System.out.println("[2] Add URL");
            System.out.println("[3] Remove URL");
            System.out.println("[4] Exit");
            int menuChooser = scanner.nextInt();
            if (menuChooser == 1)
                showUpdates();
            else if (menuChooser == 2)
                addURL();
            else if (menuChooser == 3)
                removeURL();
            else if(menuChooser == 4)
                System.exit(0);
            else {
                System.out.println("Invalid input");
                Home();
            }
        }
        catch (Exception e) {
            System.out.println("Invalid input");
            Home();
        }
    }
    public static void showUpdates() {
        try {
            //build it if doesn't exist
            FileWriter writer = new FileWriter("data.txt", true);
            writer.close();
            //looking for URL
            System.out.println("Show updates for: ");
            String[][] address = new String[200][3];
            BufferedReader reader = new BufferedReader(new FileReader("data.txt"));
            int cnt = 0;
            System.out.println("[" + cnt + "] All websites");
            String line;
            while ((line = reader.readLine()) != null) {
                address[++cnt] = line.split(";");
                System.out.println("[" + cnt + "] " + address[cnt][0]);
            }
            if (cnt == 0) {
                System.out.println("File is empty.");
                Home();
                return;
            }
            System.out.println("Enter -1 to return");
            // Show updates for selected site
            Scanner scanner = new Scanner(System.in);
            int input = scanner.nextInt();
            if (input == 0) {
                for (int i = 1; i <= cnt; i++) {
                    System.out.println(address[i][0]);
                    retrieveRssContent(address[i][2], 5);
                }
            } else if (input != -1) {
                System.out.println(address[input][0]);
                retrieveRssContent(address[input][2], 5);
            }
        }
        catch (Exception e) {
            System.out.println("Can't Show updates.\nMaybe there's a problem with your connection or site doesn't have RSS.");
        }
        Home();
    }
    public static void addURL() {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Pleas enter website URL to add.");
            FileWriter writer = new FileWriter("data.txt", true);
            String websiteURL = scanner.next();
            String addLine = extractPageTitle(fetchPageSource(websiteURL)) + ";" + websiteURL + "index.html" + ";" + extractRssUrl(websiteURL);
            BufferedReader reader = new BufferedReader(new FileReader("data.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(addLine)) {
                    System.out.println();
                    System.out.println(websiteURL + " already exist.");
                    Home();
                    return;
                }
            }
            System.out.println("Added " + websiteURL + " successfully.");
            writer.write(addLine + "\n");
            writer.close();
            reader.close();
        }
        catch (Exception ex) {
            //ex.printStackTrace();
            System.out.println("failed to add URL.\nMaybe there's problem with your connection or site address.");
        }
        Home();
    }
    public static void removeURL() {
        try {
            File data = new File("data.txt");
            System.out.println("Pleas enter website URL to remove.");
            Scanner scanner = new Scanner(System.in);
            String websiteURL = scanner.next();
            String validLine = extractPageTitle(fetchPageSource(websiteURL)) + ";" + websiteURL + "index.html" + ";" + extractRssUrl(websiteURL);
            int cnt = 0;
            int removeLine = -1;
            boolean check = false;
            BufferedReader reader = new BufferedReader(new FileReader(data));
            String[] line = new String[100];
            //check if doesn't exist
            while ((line[cnt] = reader.readLine()) != null) {
                if (line[cnt].equals(validLine)) {
                    check = true;
                    removeLine = cnt;
                }
                cnt++;
            }
            reader.close();
            if (check == false) {
                System.out.println("Couldn't find " + websiteURL);
                Home();
                return;
            }
            // remove address
            System.out.println(websiteURL + " Removed successfully");
            FileWriter writer = new FileWriter(data, false);
            writer.flush();
            writer.close();
            writer = new FileWriter(data, true);
            for (int i = 0; i < cnt; i++)
                if (i != removeLine)
                    writer.write(line[i] + "\n");
            writer.close();
        }
        catch (Exception ex) {
            System.out.println("failed to remove URL.\nMaybe there's problem with your connection or site address.");
        }
        Home();
    }
    public static String extractPageTitle(String html) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("title").first().text();
        }
        catch (Exception e)  {
            return "Error: no title tag found in page source!";
        }
    }
    public static String extractRssUrl(String url) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }
    public static String fetchPageSource(String urlString) throws Exception {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML , like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
    }
    private static String toString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream , "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);
        return stringBuilder.toString();
    }
    public static void retrieveRssContent(String rssUrl, int MAX_ITEMS) {
        try {
             String rssXml = fetchPageSource(rssUrl);
             DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
             StringBuilder xmlStringBuilder = new StringBuilder();
             xmlStringBuilder.append(rssXml);
             ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
             org.w3c.dom.Document doc = documentBuilder.parse(input);
             NodeList itemNodes = doc.getElementsByTagName("item");

            for (int i = 0; i < MAX_ITEMS; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).
                            getTextContent());
                    }
                }
             }
         catch (Exception e){
             System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
             }
         }
}