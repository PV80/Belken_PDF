import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class PdfServer {
    // Define the PDF filename as a constant
    private static final String PDF_FILENAME = "belken_document.pdf";
    private static final String HTML_FILENAME = "index.html";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/" + PDF_FILENAME, new PdfHandler()); // Use the constant
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started. Open http://localhost:8000 in your browser.");
        System.out.println("To view the PDF directly (served by Java): http://localhost:8000/" + PDF_FILENAME);
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File file = new File(HTML_FILENAME);
            if (!file.exists()) {
                String response = "Error: " + HTML_FILENAME + " not found.";
                System.err.println(response + " Current directory: " + new File(".").getAbsolutePath());
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().add("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class PdfHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File file = new File(PDF_FILENAME); // Use the constant
            if (!file.exists()) {
                String response = "Error: PDF file '" + PDF_FILENAME + "' not found.";
                System.err.println(response + " Current directory: " + new File(".").getAbsolutePath());
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().add("Content-Type", "application/pdf");
            // Optional: Suggests the browser display it inline rather than download
            exchange.getResponseHeaders().add("Content-Disposition", "inline; filename=\"" + PDF_FILENAME + "\"");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}