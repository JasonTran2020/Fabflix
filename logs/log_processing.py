import math

class log_parser:
    def __init__(self):
        self.total_TS = 0
        self.total_TJ = 0
        self.count = 0

    def parse_log(self):
        file_path = input("Please enter in the path to the log file: ")
        file = open(file_path, "r")
        for line in file:
            temp = line.split(sep=",")

            TS_sentence = temp[0]
            TJ_sentence = temp[1]
            current_TS = int(TS_sentence[TS_sentence.index(":") + 1:])
            current_TJ = int(TJ_sentence[TJ_sentence.index(":") + 1:])

            self.count += 1
            self.total_TS += current_TS
            self.total_TJ += current_TJ
        avg_TS_time = math.trunc(self.total_TS / self.count)
        avg_TJ_time = math.trunc(self.total_TJ / self.count)
        print("Total number of requests: " + str(self.count))
        print("Average TS time in nanoseconds: " + str(avg_TS_time))
        print("Average TJ time in nanoseconds: " + str(avg_TJ_time))

        print("Average TS time in milliseconds: " + str(avg_TS_time / 1000000))
        print("Average TJ time in milliseconds: " + str(avg_TJ_time / 1000000))

if __name__ == '__main__':

    parser = log_parser()
    while True:
        parser.parse_log()
        decision = input("Parse another file and combine statistics? (y/n)")
        if (decision=="y"):
            continue
        break






# See PyCharm help at https://www.jetbrains.com/help/pycharm/