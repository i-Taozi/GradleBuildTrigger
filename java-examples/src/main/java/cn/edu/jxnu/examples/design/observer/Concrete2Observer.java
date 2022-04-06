/* All Contributors (C) 2020 */
package cn.edu.jxnu.examples.design.observer;

// 具体的观察者2
class Concrete2Observer extends Observer {

    public Concrete2Observer(Subject subject) {
        this.subject = subject;
        this.subject.addObserver(this);
    }

    @Override
    public void update() {
        System.out.println("Concrete2Observer println world");
    }
}
