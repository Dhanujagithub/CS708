from flask import Flask
from flask import request
import json
import csv
from module_solver_cs708 import *
#from timeit import default_timer as timer
import os

#net=init_seg_model()
app = Flask(__name__)

@app.route("/", methods=['GET'])
def home():
    return "Hello, World!"

@app.route('/detect_target', methods = ['POST'])
def detect_target():
    if request.method == 'POST':
        data = request.files['fileToUpload']
        depth_image = request.form['depth_image']
        translate_matrix = request.form['translate_matrix']
        camera_matrix = request.form['camera_matrix']
        command = request.form['command']
        image_env= "scene_environment.png"
        data.save("./images/image/"+image_env)

        file1 = open("./images/depth/depth0.txt", 'w')
        for i in depth_image.split("\n"):
         if(len(i)>0):
           file1.writelines("% s\n" %i)
        file1.close()
        
        file = open("./images/trans_matrix/matrix0.txt", 'w')
        for i in translate_matrix.split("\n"):
         if(len(i)>0):
           file.writelines("% s\n" %i)
        file.close()

        file = open("./images/intric_matrix/matrix0.txt", 'w')
        for i in camera_matrix.split("\n"):
         if(len(i)>0):
           file.writelines("% s\n" %i)
        file.close()

        file = open("./images/command/command0.txt", 'w')
        file.writelines(command)
        file.close()

        net=init_seg_model()
        output= module_sol(net)
    return str(output)



@app.route('/quit')
def _quit():
    os._exit(0)

if __name__ == "__main__":
    app.run(host='172.17.161.118',port=5000) #192.168.18.12 # 192.168.10.148 10.119.133.118