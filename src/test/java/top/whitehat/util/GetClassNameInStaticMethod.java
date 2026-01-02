/*
 * Copyright 2026 The WhiteHat Project
 *
 * The WhiteHat Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package top.whitehat.util;

import java.lang.invoke.MethodHandles;

public class GetClassNameInStaticMethod {
    
    /**
     * 方法1：使用 new Object(){}.getClass().getEnclosingClass()
     * 通过匿名内部类获取外围类
     */
    public static String getClassNameMethod1() {
        return new Object(){}.getClass().getEnclosingClass().getName();
    }
    
    /**
     * 方法2：使用 Thread.currentThread().getStackTrace()
     * 通过调用栈信息获取类名
     */
    public static String getClassNameMethod2() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // stackTrace[0] 是 getStackTrace 方法本身
        // stackTrace[1] 是当前方法 (getClassNameMethod2)
        // stackTrace[2] 是调用当前方法的方法
        return stackTrace[1].getClassName();
    }
    
    /**
     * 方法3：使用 MethodHandles.lookup().lookupClass() (Java 7+)
     * 这是最简洁和推荐的方式
     */
    public static String getClassNameMethod3() {
        return MethodHandles.lookup().lookupClass().getName();
    }
    
    /**
     * 方法4：获取简单类名（不包含包名）
     */
    public static String getSimpleClassName() {
        return MethodHandles.lookup().lookupClass().getSimpleName();
    }
    
    /**
     * 测试方法
     */
    public static void main(String[] args) {
        System.out.println("方法1获取的类名: " + getClassNameMethod1());
        System.out.println("方法2获取的类名: " + getClassNameMethod2());
        System.out.println("方法3获取的类名: " + getClassNameMethod3());
        System.out.println("简单类名: " + getSimpleClassName());
        
        // 验证所有方法获取的类名是否一致
        String class1 = getClassNameMethod1();
        String class2 = getClassNameMethod2();
        String class3 = getClassNameMethod3();
        
        System.out.println("\n验证结果:");
        System.out.println("方法1和方法2结果一致: " + class1.equals(class2));
        System.out.println("方法1和方法3结果一致: " + class1.equals(class3));
        System.out.println("方法2和方法3结果一致: " + class2.equals(class3));
    }
}

