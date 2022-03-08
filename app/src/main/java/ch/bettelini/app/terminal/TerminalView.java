package ch.bettelini.app.terminal;

public abstract class TerminalView {
    
    private TerminalApplication app;

    protected TerminalView parentView;
    
    protected abstract void render(); // Draw view
    protected abstract void input(String input); // User input
    protected abstract void onDisplay(); // When this view is display
    protected abstract void onConceal(); // When this view is concealed

    public void setParent(TerminalView parentView) {
        this.parentView = parentView;
    }

    public TerminalView getParentView() {
        return parentView;
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

    protected boolean updateIfCurrent(TerminalView view) {
        return app.updateIfCurrent(view);
    }

}