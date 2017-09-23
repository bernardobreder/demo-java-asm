package emuasm.task.util;

import java.awt.Component;

import javax.swing.SwingUtilities;

public abstract class Task implements Runnable {

  private Thread thread;

  protected Component parentComponent;

  protected Object result;

  public abstract void perform() throws Throwable;

  public abstract void updateUI();

  public void doneUI() {
  }

  public void handlerUI(Throwable e) {
    e.printStackTrace();
  }

  protected abstract void createProgressFrame();

  protected abstract void showProgressFrame();

  protected abstract void closeProgressFrame();

  public Task start() {
    this.thread = new Thread(this);
    createProgressFrame();
    this.thread.start();
    try {
      this.thread.join(200);
    }
    catch (InterruptedException e) {
    }
    if (this.thread.isAlive()) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          showProgressFrame();
        }
      });
    }
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void run() {
    try {
      perform();
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          updateUI();
        }
      });
    }
    catch (final Throwable e) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          handlerUI(e);
        }
      });
    }
    finally {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          closeProgressFrame();
        }
      });
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          doneUI();
        }
      });
    }
  }

  public Task setParentComponent(Component parentComponent) {
    this.parentComponent = parentComponent;
    return this;
  }

  public Component getParentComponent() {
    return parentComponent;
  }

  @SuppressWarnings("unchecked")
  public <E> E getResult() {
    return (E) result;
  }

  public void setResult(Object result) {
    this.result = result;
  }

}
