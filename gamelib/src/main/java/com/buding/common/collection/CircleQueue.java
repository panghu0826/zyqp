package com.buding.common.collection;

import java.util.Arrays;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

/**
 * @Created Apr 7, 2015 11:06:21 AM
 * @Description
 *              <p>
 *              创建一个循环队列（环形缓冲、RingBuffer），实际元素存在一个数组中，操作数组的指针，不移动元素
 */
public class CircleQueue<T> {

	/**
	 * 循环队列 （数组）默认大小
	 */
	private final int DEFAULT_SIZE = 1000;

	/**
	 * (循环队列)数组的容量
	 */
	public int capacity;

	/**
	 * 数组：保存循环队列的元素
	 */
	public Object[] elementData;

	/**
	 * 队头(先见先出)
	 */
	public int head = 0;

	/**
	 * 队尾
	 */
	public int tail = 0;

	/**
	 * 以循环队列 默认大小创建空循环队列
	 */
	public CircleQueue() {
		capacity = DEFAULT_SIZE;
		elementData = new Object[capacity];
	}

	/**
	 * 以指定长度的数组来创建循环队列
	 * 
	 * @param initSize
	 */
	public CircleQueue(final int initSize) {
		capacity = initSize;
		elementData = new Object[capacity];
	}

	/**
	 * 获取循环队列的大小(包含元素的个数)
	 */
	public int size() {
		if (isEmpty()) {
			return 0;
		} else if (isFull()) {
			return capacity;
		} else {
			return tail + 1;
		}
	}

	/**
	 * 插入队尾一个元素
	 */
	public void add(final T element) {
		if (isEmpty()) {
			elementData[0] = element;
		} else if (isFull()) {
			elementData[head] = element;
			head++;
			tail++;
			head = head == capacity ? 0 : head;
			tail = tail == capacity ? 0 : tail;
		} else {
			elementData[tail + 1] = element;
			tail++;
		}
	}

	public boolean isEmpty() {
		return tail == head && tail == 0 && elementData[tail] == null;
	}

	public boolean isFull() {
		return (head != 0 && head - tail == 1) || (head == 0 && tail == capacity - 1);
	}

	public void clear() {
		Arrays.fill(elementData, null);
		head = 0;
		tail = 0;
	}

	/**
	 * @return 取 循环队列里的值（先进的index=0）
	 */
	public Object[] getQueue() {
		final Object[] elementDataSort = new Object[capacity];
		final Object[] elementDataCopy = elementData.clone();
		if (isEmpty()) {
		} else if (isFull()) {
			int indexMax = capacity;
			int indexSort = 0;
			for (int i = head; i < indexMax;) {
				elementDataSort[indexSort] = elementDataCopy[i];
				indexSort++;
				i++;
				if (i == capacity) {
					i = 0;
					indexMax = head;
				}
			}
		} else {
			// elementDataSort = elementDataCopy;//用这行代码代替下面的循环，在队列刚满时候会出错
			for (int i = 0; i < tail; i++) {
				elementDataSort[i] = elementDataCopy[i];
			}
		}
		return elementDataSort;
	}

	/**
	 * 测试代码
	 */
	public static void main(final String[] args) {
		final CircleQueue<Integer> queue = new CircleQueue<Integer>(10);
		for (int i = 0; i < 99; i++) {
			queue.add(i);
		}

		final Object[] queueArray = queue.getQueue();
		System.out.println("按入队列的先后顺序打印出来：");
		for (final Object o : queueArray) {
			System.out.println(o);
		}
		System.out.println("capacity: " + queue.capacity);
		System.out.println("size: " + queue.size());
		System.out.println("head index: " + queue.head);
		System.out.println("tail index: " + queue.tail);

		
		ConcurrentLinkedHashMap<Integer, Integer> OBJECT_MAPS = new ConcurrentLinkedHashMap.Builder<Integer, Integer>().maximumWeightedCapacity(3).build();
		
	}
}
