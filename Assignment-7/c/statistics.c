#include <stdio.h>
#include <stdlib.h>

static int compare_ints(const void *a, const void *b) {
    int x = *(const int *)a;
    int y = *(const int *)b;
    return (x > y) - (x < y);
}

double calculate_mean(const int *arr, int n) {
    long long sum = 0;
    int i;
    for (i = 0; i < n; i++) {
        sum += arr[i];
    }
    return (double)sum / (double)n;
}

double calculate_median(int *arr, int n) {
    qsort(arr, n, sizeof(int), compare_ints);

    if (n % 2 == 1) {
        return (double)arr[n / 2];
    }
    return ((double)arr[(n / 2) - 1] + (double)arr[n / 2]) / 2.0;
}

void calculate_mode(const int *arr, int n) {
    int *copy = (int *)malloc((size_t)n * sizeof(int));
    int i, max_freq = 0, current_freq = 1;
    int first = 1;

    if (copy == NULL) {
        printf("Mode: memory allocation failed\n");
        return;
    }

    for (i = 0; i < n; i++) {
        copy[i] = arr[i];
    }
    qsort(copy, n, sizeof(int), compare_ints);

    for (i = 1; i <= n; i++) {
        if (i < n && copy[i] == copy[i - 1]) {
            current_freq++;
        } else {
            if (current_freq > max_freq) {
                max_freq = current_freq;
            }
            current_freq = 1;
        }
    }

    if (max_freq == 1) {
        printf("Mode: no mode (all values unique)\n");
        free(copy);
        return;
    }

    printf("Mode: ");
    current_freq = 1;
    for (i = 1; i <= n; i++) {
        if (i < n && copy[i] == copy[i - 1]) {
            current_freq++;
        } else {
            if (current_freq == max_freq) {
                if (!first) {
                    printf(", ");
                }
                printf("%d", copy[i - 1]);
                first = 0;
            }
            current_freq = 1;
        }
    }
    printf(" (frequency %d)\n", max_freq);

    free(copy);
}

int main(void) {
    FILE *fp = fopen("../tests/sample_input.txt", "r");
    int capacity = 16;
    int count = 0;
    int *values;
    int *median_copy;
    int i;

    if (fp == NULL) {
        printf("Error: could not open ../tests/sample_input.txt\n");
        return 1;
    }

    values = (int *)malloc((size_t)capacity * sizeof(int));
    if (values == NULL) {
        fclose(fp);
        printf("Error: memory allocation failed\n");
        return 1;
    }

    while (1) {
        int x;
        if (fscanf(fp, "%d", &x) != 1) {
            break;
        }
        if (count == capacity) {
            int *tmp;
            capacity *= 2;
            tmp = (int *)realloc(values, (size_t)capacity * sizeof(int));
            if (tmp == NULL) {
                free(values);
                fclose(fp);
                printf("Error: memory reallocation failed\n");
                return 1;
            }
            values = tmp;
        }
        values[count++] = x;
    }
    fclose(fp);

    if (count == 0) {
        free(values);
        printf("No integers found in input.\n");
        return 1;
    }

    median_copy = (int *)malloc((size_t)count * sizeof(int));
    if (median_copy == NULL) {
        free(values);
        printf("Error: memory allocation failed\n");
        return 1;
    }

    for (i = 0; i < count; i++) {
        median_copy[i] = values[i];
    }

    printf("C Procedural Statistics\n");
    printf("Input count: %d\n", count);
    printf("Mean: %.2f\n", calculate_mean(values, count));
    printf("Median: %.2f\n", calculate_median(median_copy, count));
    calculate_mode(values, count);

    free(median_copy);
    free(values);
    return 0;
}
