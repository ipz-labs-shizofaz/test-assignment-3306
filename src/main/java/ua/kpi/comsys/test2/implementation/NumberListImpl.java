/*
 * Copyright (c) 2014-2025, NTUU KPI, Computer systems department and/or its affiliates. All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package ua.kpi.comsys.test2.implementation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import ua.kpi.comsys.test2.NumberList;

/**
 * Custom implementation of NumberList interface that stores digits
 * inside a linear doubly-linked list.
 *
 * @author Herasymchuk Danyil IO-33 #6
 */
public class NumberListImpl implements NumberList {

    private static final int RECORD_BOOK_NUMBER = 6;
    private static final int PRIMARY_RADIX = 3;
    private static final int SECONDARY_RADIX = 8;
    private static final int MIN_RADIX = 2;
    private static final int MAX_RADIX = 36;
    private static final String DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final int radix;
    private Node head;
    private Node tail;
    private int size;
    private int modCount;

    private static final class Node {

        byte value;
        Node next;
        Node prev;

        Node(byte value) {
            this.value = value;
        }
    }

    /**
     * Default constructor. Returns empty <tt>NumberListImpl</tt>
     */
    public NumberListImpl() {
        this(PRIMARY_RADIX);
    }

    private NumberListImpl(int radix) {
        if (radix < MIN_RADIX || radix > MAX_RADIX) {
            throw new IllegalArgumentException("Unsupported radix: " + radix);
        }
        this.radix = radix;
    }

    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * from file, defined in string format.
     *
     * @param file - file where number is stored.
     */
    public NumberListImpl(File file) {
        this();
        if (file == null || !file.exists() || !file.isFile()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) {
                initializeFromDecimalString(line.trim());
            }
        } catch (IOException ex) {
            clear();
        }
    }

    /**
     * Constructs new <tt>NumberListImpl</tt> by <b>decimal</b> number
     * in string notation.
     *
     * @param value - number in string notation.
     */
    public NumberListImpl(String value) {
        this();
        initializeFromDecimalString(value);
    }

    /**
     * Saves the number, stored in the list, into specified file
     * in <b>decimal</b> scale of notation.
     *
     * @param file - file where number has to be stored.
     */
    public void saveList(File file) {
        if (file == null) {
            return;
        }
        try (
            BufferedWriter writer = new BufferedWriter(
                new FileWriter(file, false)
            )
        ) {
            String decimal = toDecimalString();
            if (!decimal.isEmpty()) {
                writer.write(decimal);
            }
        } catch (IOException ignored) {}
    }

    /**
     * Returns student's record book number.
     *
     * @return student's record book number.
     */
    public static int getRecordBookNumber() {
        return RECORD_BOOK_NUMBER;
    }

    /**
     * Returns new <tt>NumberListImpl</tt> which represents the same number
     * in other scale of notation.<p>
     *
     * @return <tt>NumberListImpl</tt> in other scale of notation.
     */
    public NumberListImpl changeScale() {
        BigInteger value = toBigInteger();
        NumberListImpl converted = new NumberListImpl(SECONDARY_RADIX);
        converted.populateFromBigInteger(value);
        return converted;
    }

    /**
     * Returns new <tt>NumberListImpl</tt> which represents the result of
     * additional operation.<p>
     *
     * @param arg - second argument of additional operation
     *
     * @return result of additional operation.
     */
    public NumberListImpl additionalOperation(NumberList arg) {
        BigInteger left = toBigInteger();
        BigInteger right = numberListToBigInteger(arg);
        BigInteger resultValue = left.or(right);
        NumberListImpl result = new NumberListImpl(this.radix);
        result.populateFromBigInteger(resultValue);
        return result;
    }

    /**
     * Returns string representation of number, stored in the list
     * in <b>decimal</b> scale of notation.
     *
     * @return string representation in <b>decimal</b> scale.
     */
    public String toDecimalString() {
        return toBigInteger().toString();
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(size);
        Node current = head;
        while (current != null) {
            builder.append(toDigitChar(current.value));
            current = current.next;
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NumberListImpl)) {
            return false;
        }
        NumberListImpl other = (NumberListImpl) o;
        if (this.radix != other.radix || this.size != other.size) {
            return false;
        }
        Node left = this.head;
        Node right = other.head;
        while (left != null && right != null) {
            if (left.value != right.value) {
                return false;
            }
            left = left.next;
            right = right.next;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        Node current = head;
        while (current != null) {
            hash = 31 * hash + current.value;
            current = current.next;
        }
        hash = 31 * hash + radix;
        return hash;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Byte)) {
            return false;
        }
        byte target = (Byte) o;
        Node current = head;
        while (current != null) {
            if (current.value == target) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    @Override
    public Iterator<Byte> iterator() {
        return listIterator();
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size];
        Node node = head;
        int index = 0;
        while (node != null) {
            array[index++] = node.value;
            node = node.next;
        }
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException(
            "Generic toArray is not implemented."
        );
    }

    @Override
    public boolean add(Byte e) {
        Objects.requireNonNull(e, "Digit must not be null");
        checkDigitRange(e);
        linkLast(e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Byte)) {
            return false;
        }
        byte target = (Byte) o;
        Node current = head;
        while (current != null) {
            if (current.value == target) {
                unlink(current);
                return true;
            }
            current = current.next;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Objects.requireNonNull(c);
        for (Object obj : c) {
            if (!contains(obj)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        return addAll(size, c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        Objects.requireNonNull(c);
        checkPositionIndex(index);
        Object[] data = c.toArray();
        if (data.length == 0) {
            return false;
        }
        Node successor = (index == size) ? null : nodeAt(index);
        Node predecessor = (index == size) ? tail : successor.prev;
        for (Object value : data) {
            Byte element = (Byte) Objects.requireNonNull(
                value,
                "Digit must not be null"
            );
            checkDigitRange(element);
            Node newNode = new Node(element);
            if (predecessor == null) {
                head = newNode;
            } else {
                predecessor.next = newNode;
            }
            newNode.prev = predecessor;
            predecessor = newNode;
            if (successor != null) {
                newNode.next = successor;
            }
            size++;
            modCount++;
        }
        if (successor == null) {
            tail = predecessor;
        } else {
            successor.prev = predecessor;
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Node current = head;
        while (current != null) {
            Node next = current.next;
            if (c.contains(current.value)) {
                unlink(current);
                modified = true;
            }
            current = next;
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Node current = head;
        while (current != null) {
            Node next = current.next;
            if (!c.contains(current.value)) {
                unlink(current);
                modified = true;
            }
            current = next;
        }
        return modified;
    }

    @Override
    public void clear() {
        Node current = head;
        while (current != null) {
            Node next = current.next;
            current.next = null;
            current.prev = null;
            current = next;
        }
        head = null;
        tail = null;
        if (size != 0) {
            size = 0;
            modCount++;
        }
    }

    @Override
    public Byte get(int index) {
        checkElementIndex(index);
        return nodeAt(index).value;
    }

    @Override
    public Byte set(int index, Byte element) {
        Objects.requireNonNull(element, "Digit must not be null");
        checkDigitRange(element);
        checkElementIndex(index);
        Node node = nodeAt(index);
        byte old = node.value;
        node.value = element;
        return old;
    }

    @Override
    public void add(int index, Byte element) {
        Objects.requireNonNull(element, "Digit must not be null");
        checkDigitRange(element);
        checkPositionIndex(index);
        if (index == size) {
            linkLast(element);
        } else {
            linkBefore(element, nodeAt(index));
        }
    }

    @Override
    public Byte remove(int index) {
        checkElementIndex(index);
        Node node = nodeAt(index);
        return unlink(node);
    }

    @Override
    public int indexOf(Object o) {
        if (!(o instanceof Byte)) {
            return -1;
        }
        byte target = (Byte) o;
        Node node = head;
        int idx = 0;
        while (node != null) {
            if (node.value == target) {
                return idx;
            }
            idx++;
            node = node.next;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof Byte)) {
            return -1;
        }
        byte target = (Byte) o;
        Node node = tail;
        int idx = size - 1;
        while (node != null) {
            if (node.value == target) {
                return idx;
            }
            idx--;
            node = node.prev;
        }
        return -1;
    }

    @Override
    public ListIterator<Byte> listIterator() {
        return new ListItr(0);
    }

    @Override
    public ListIterator<Byte> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(index);
    }

    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        NumberListImpl result = new NumberListImpl(this.radix);
        Node node = (fromIndex == size) ? null : nodeAt(fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            result.add(node.value);
            node = node.next;
        }
        return result;
    }

    @Override
    public boolean swap(int index1, int index2) {
        if (index1 == index2) {
            return true;
        }
        if (!isElementIndex(index1) || !isElementIndex(index2)) {
            return false;
        }
        Node node1 = nodeAt(index1);
        Node node2 = nodeAt(index2);
        byte tmp = node1.value;
        node1.value = node2.value;
        node2.value = tmp;
        return true;
    }

    @Override
    public void sortAscending() {
        if (size < 2) {
            return;
        }
        int[] counts = new int[radix];
        Node node = head;
        while (node != null) {
            counts[node.value]++;
            node = node.next;
        }
        node = head;
        for (int digit = 0; digit < radix; digit++) {
            int count = counts[digit];
            for (int i = 0; i < count; i++) {
                node.value = (byte) digit;
                node = node.next;
            }
        }
    }

    @Override
    public void sortDescending() {
        if (size < 2) {
            return;
        }
        int[] counts = new int[radix];
        Node node = head;
        while (node != null) {
            counts[node.value]++;
            node = node.next;
        }
        node = head;
        for (int digit = radix - 1; digit >= 0; digit--) {
            int count = counts[digit];
            for (int i = 0; i < count; i++) {
                node.value = (byte) digit;
                node = node.next;
            }
        }
    }

    @Override
    public void shiftLeft() {
        if (size <= 1) {
            return;
        }
        Node first = head;
        head = first.next;
        head.prev = null;
        tail.next = first;
        first.prev = tail;
        first.next = null;
        tail = first;
        modCount++;
    }

    @Override
    public void shiftRight() {
        if (size <= 1) {
            return;
        }
        Node last = tail;
        tail = last.prev;
        tail.next = null;
        last.prev = null;
        last.next = head;
        head.prev = last;
        head = last;
        modCount++;
    }

    private void initializeFromDecimalString(String value) {
        clear();
        if (value == null) {
            return;
        }
        String normalized = normalizeDecimal(value);
        if (normalized == null || normalized.isEmpty()) {
            return;
        }
        BigInteger number = new BigInteger(normalized);
        if (number.signum() < 0) {
            return;
        }
        populateFromBigInteger(number);
    }

    private String normalizeDecimal(String value) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("+")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.startsWith("-")) {
            return null;
        }
        for (int i = 0; i < trimmed.length(); i++) {
            if (!Character.isDigit(trimmed.charAt(i))) {
                return null;
            }
        }
        int firstNonZero = 0;
        while (
            firstNonZero < trimmed.length() - 1 &&
            trimmed.charAt(firstNonZero) == '0'
        ) {
            firstNonZero++;
        }
        return trimmed.substring(firstNonZero);
    }

    private void populateFromBigInteger(BigInteger number) {
        clear();
        if (number == null || number.signum() < 0) {
            return;
        }
        if (number.equals(BigInteger.ZERO)) {
            linkLast((byte) 0);
            return;
        }
        BigInteger current = number;
        BigInteger base = BigInteger.valueOf(radix);
        byte[] buffer = new byte[Math.max(4, current.bitLength() + 1)];
        int length = 0;
        while (current.signum() > 0) {
            BigInteger[] divRem = current.divideAndRemainder(base);
            if (length == buffer.length) {
                byte[] newBuffer = new byte[buffer.length * 2];
                System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                buffer = newBuffer;
            }
            buffer[length++] = divRem[1].byteValue();
            current = divRem[0];
        }
        for (int i = length - 1; i >= 0; i--) {
            linkLast(buffer[i]);
        }
    }

    private void linkLast(byte value) {
        Node newNode = new Node(value);
        Node previous = tail;
        tail = newNode;
        if (previous == null) {
            head = newNode;
        } else {
            previous.next = newNode;
            newNode.prev = previous;
        }
        size++;
        modCount++;
    }

    private void linkBefore(byte value, Node node) {
        Node previous = node.prev;
        Node newNode = new Node(value);
        newNode.next = node;
        newNode.prev = previous;
        node.prev = newNode;
        if (previous == null) {
            head = newNode;
        } else {
            previous.next = newNode;
        }
        size++;
        modCount++;
    }

    private Byte unlink(Node node) {
        byte element = node.value;
        Node next = node.next;
        Node prev = node.prev;
        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
        }
        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
        }
        node.next = null;
        node.prev = null;
        size--;
        modCount++;
        if (size == 0) {
            head = null;
            tail = null;
        }
        return element;
    }

    private void checkDigitRange(byte value) {
        if (value < 0 || value >= radix) {
            throw new IllegalArgumentException(
                "Digit " + value + " is out of range for radix " + radix
            );
        }
    }

    private Node nodeAt(int index) {
        if (index < (size >> 1)) {
            Node node = head;
            for (int i = 0; i < index; i++) {
                node = node.next;
            }
            return node;
        } else {
            Node node = tail;
            for (int i = size - 1; i > index; i--) {
                node = node.prev;
            }
            return node;
        }
    }

    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

    private void checkElementIndex(int index) {
        if (!isElementIndex(index)) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + size
            );
        }
    }

    private void checkPositionIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + size
            );
        }
    }

    private char toDigitChar(byte digit) {
        return DIGITS.charAt(digit);
    }

    private BigInteger toBigInteger() {
        if (isEmpty()) {
            return BigInteger.ZERO;
        }
        BigInteger base = BigInteger.valueOf(radix);
        BigInteger total = BigInteger.ZERO;
        Node node = head;
        while (node != null) {
            total = total.multiply(base).add(BigInteger.valueOf(node.value));
            node = node.next;
        }
        return total;
    }

    private static BigInteger numberListToBigInteger(NumberList list) {
        if (list == null) {
            return BigInteger.ZERO;
        }
        if (list instanceof NumberListImpl) {
            return ((NumberListImpl) list).toBigInteger();
        }
        BigInteger base = BigInteger.TEN;
        BigInteger total = BigInteger.ZERO;
        for (Byte digit : list) {
            if (digit == null) {
                continue;
            }
            total = total.multiply(base).add(BigInteger.valueOf(digit));
        }
        return total;
    }

    private class ListItr implements ListIterator<Byte> {

        private Node next;
        private Node lastReturned;
        private int nextIndex;
        private int expectedModCount = modCount;

        ListItr(int index) {
            if (index == size) {
                next = null;
            } else {
                next = nodeAt(index);
            }
            nextIndex = index;
        }

        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }

        @Override
        public Byte next() {
            checkForComodification();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.value;
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public Byte previous() {
            checkForComodification();
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            if (next == null) {
                next = tail;
            } else {
                next = next.prev;
            }
            lastReturned = next;
            nextIndex--;
            return lastReturned.value;
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            checkForComodification();
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            Node lastNext = lastReturned.next;
            unlink(lastReturned);
            if (next == lastReturned) {
                next = lastNext;
            } else {
                nextIndex--;
            }
            lastReturned = null;
            expectedModCount = modCount;
        }

        @Override
        public void set(Byte e) {
            Objects.requireNonNull(e, "Digit must not be null");
            checkDigitRange(e);
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            lastReturned.value = e;
        }

        @Override
        public void add(Byte e) {
            Objects.requireNonNull(e, "Digit must not be null");
            checkDigitRange(e);
            checkForComodification();
            lastReturned = null;
            if (next == null) {
                linkLast(e);
                next = null;
            } else {
                linkBefore(e, next);
            }
            nextIndex++;
            expectedModCount = modCount;
        }

        private void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
