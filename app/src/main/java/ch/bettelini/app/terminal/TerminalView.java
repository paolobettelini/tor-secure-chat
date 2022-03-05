package ch.bettelini.app.terminal;

public abstract class TerminalView {
    
    private TerminalApplication app;
    protected TerminalView parentView;

    protected abstract void input(String input);
    protected abstract void render();

    public void setParent(TerminalView parentView) {
        this.parentView = parentView;
    }
    
    void setTerminalApplication(TerminalApplication app) {
        this.app = app;
    }

    public void show() {
        render();
        this.app.setView(this);
    }

    protected void setView(TerminalView view) {
        app.setView(view);
    }

    protected void setView(TerminalView view, TerminalView parentView) {
        app.setView(view, parentView);
    }

    protected void clear() {
        app.clear();
    }

    protected void newLine() {
        app.newLine();
    }

    protected void println(String v) {
        app.println(v);
    }

    protected void print(String v) {
        app.print(v);
    }

    protected void update() {
        app.update();
    }

    protected void update(TerminalView view) {
        app.update(view);
    }

}