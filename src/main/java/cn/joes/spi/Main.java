package cn.joes.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Created by myijoes on 2019/8/2.
 *
 * @author wanqiao
 */
public class Main {

    public static void main(String[] args) {
        ServiceLoader<Person> personLoader = ServiceLoader.load(Person.class);
        Iterator<Person> iterator = personLoader.iterator();
        while (iterator.hasNext()){
            Person next = iterator.next();
            next.whoareyou();
        }
    }

}
