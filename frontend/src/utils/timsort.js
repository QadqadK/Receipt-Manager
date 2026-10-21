const RUN = 32;

function insertionSort(arr, left, right, compareFn) {
  for (let i = left + 1; i <= right; i++) {
    const key = arr[i];
    let j = i - 1;
    while (j >= left && compareFn(arr[j], key) > 0) {
      arr[j + 1] = arr[j];
      j--;
    }
    arr[j + 1] = key;
  }
}

function merge(arr, left, mid, right, compareFn) {
  const leftPart = arr.slice(left, mid + 1);
  const rightPart = arr.slice(mid + 1, right + 1);

  let i = 0;
  let j = 0;
  let k = left;

  while (i < leftPart.length && j < rightPart.length) {
    if (compareFn(leftPart[i], rightPart[j]) <= 0) {
      arr[k] = leftPart[i];
      i++;
    } else {
      arr[k] = rightPart[j];
      j++;
    }
    k++;
  }

  while (i < leftPart.length) {
    arr[k] = leftPart[i];
    i++;
    k++;
  }

  while (j < rightPart.length) {
    arr[k] = rightPart[j];
    j++;
    k++;
  }
}

/**
 * Timsort: insertion-sorts small runs, then merges them bottom-up.
 * Returns a new sorted array; does not mutate the input.
 */
export function timSort(input, compareFn = (a, b) => (a < b ? -1 : a > b ? 1 : 0)) {
  const arr = input.slice();
  const n = arr.length;

  for (let start = 0; start < n; start += RUN) {
    const end = Math.min(start + RUN - 1, n - 1);
    insertionSort(arr, start, end, compareFn);
  }

  for (let size = RUN; size < n; size *= 2) {
    for (let left = 0; left < n; left += 2 * size) {
      const mid = Math.min(left + size - 1, n - 1);
      const right = Math.min(left + 2 * size - 1, n - 1);
      if (mid < right) {
        merge(arr, left, mid, right, compareFn);
      }
    }
  }

  return arr;
}
