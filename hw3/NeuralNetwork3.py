import numpy as np
import scipy.special
import csv


def sigmoid(z):

    return 1 / (1 + np.exp(-z))


def softmax(z):
    
    max_row = np.max(z, axis=-1, keepdims=True) 
    tmp = z - max_row
    return np.exp(tmp) / np.sum(np.exp(tmp), axis=-1, keepdims=True)


def load_data(path):
    f = open(path, 'r')
    data = f.readlines()
    f.close()
    return data


class NeuralNetwork:
    def __init__(self,input_nodes, hidden_nodes, output_nodes, learning_rate):
        self.lr = learning_rate
        self.i = input_nodes
        self.h = hidden_nodes
        self.o = output_nodes
        self.iandh = np.random.normal(0.0, pow(self.h, -0.5), (self.h, self.i))
        self.hando = np.random.normal(0.0, pow(self.o, -0.5), (self.o, self.h))
        self.activation_function = lambda x: scipy.special.expit(x)
        pass

    def train(self, train_image, train_label):
        input_numbers = np.array(train_label, ndmin=2).T
        input_inputs = np.array(train_image, ndmin=2).T

        hidden_inputs = np.dot(self.iandh, input_inputs)
        hidden_outputs = self.activation_function(hidden_inputs)

        output_inputs = np.dot(self.hando, hidden_outputs)
        output_outputs = self.activation_function(output_inputs)

        output_errors = input_numbers - output_outputs
        hidden_errors = np.dot(self.hando.T, output_errors)

        self.iandh += self.lr * np.dot((hidden_errors * hidden_outputs * (1 - hidden_outputs)),
                                          np.transpose(input_inputs))

        self.hando += self.lr*np.dot((output_errors*output_outputs*(1-output_outputs)),
                                        np.transpose(hidden_outputs))

        pass

    def test(self, test_image):
        input_inputs = np.array(test_image, ndmin=2).T
        hidden_inputs = np.dot(self.iandh, input_inputs)
        hidden_outputs = self.activation_function(hidden_inputs)
        final_inputs = np.dot(self.hando,hidden_outputs)
        final_outputs = self.activation_function(final_inputs)
        return final_outputs


if __name__ == "__main__":
    input_nodes = 784
    hidden_nodes = 200
    output_nodes = 10
    learning_rate = 0.1
    epochs = 5

    n = NeuralNetwork(input_nodes, hidden_nodes, output_nodes, learning_rate)

    # get data from csv file
    path1 = 'train_label.csv'
    path2 = 'train_image.csv'
    path3 = 'test_image.csv'
    train_label = load_data(path1)
    train_image = load_data(path2)
    test_image = load_data(path3)

    for e in range(epochs):
        for label, image in zip(train_label, train_image):
            i = image.split(',')
            numbers = np.zeros(output_nodes)+0.01
            numbers[int(label[0])] = 0.99
            inputs = (np.asfarray(i[0:]) / 255 * 0.99 + 0.01)
            # train the data
            n.train(inputs, numbers)
            pass
        pass

    #writing data from training example
    f = open('test_predictions.csv', 'w', encoding='utf-8')
    csv_writer = csv.writer(f)
    for image in test_image:
        i = image.split(',')
        inputs = (np.asfarray(i[0:])/255*0.99)+0.01
        outputs = n.test(inputs)
        label = np.argmax(outputs)
        csv_writer.writerow([label])
    f.close()



