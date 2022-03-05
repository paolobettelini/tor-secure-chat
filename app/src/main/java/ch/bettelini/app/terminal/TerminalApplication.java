package ch.bettelini.app.terminal;

import java.util.Scanner;

public class TerminalApplication {
    
    private String title;
    private TerminalView currentView;

    public TerminalApplication(String title) {
        this.title = title + "\r\n";
    }

    public void addView(TerminalView view) {
        view.setTerminalApplication(this);
    }

    public void setView(TerminalView view) {
        currentView = view;
        view.render();
    }

    public void setView(TerminalView view, TerminalView parentView) {
        currentView = view;
        currentView.setParent(parentView);
        currentView.render();
    }

    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNext()) {
                if (currentView != null) {
                    currentView.input(scanner.nextLine());
                }
            }
        }
    }

    public void update() {
        if (currentView != null) {
            currentView.render();
        }
    }

    public void update(TerminalView view) {
        if (currentView == view) {
            currentView.render();
        }
    }

    void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        System.out.println(title);
    }

    void newLine() {
        System.out.println();
    }

    void println(String v) {
        System.out.println(v);
    }

    void print(String v) {
        System.out.print(v);
    }

}
