all: compile-server-rmi compile-middleware

RMIInterface.jar: Server/Interface/IResourceManager.java
	@echo "Compiling RMI server interface"
	javac Server/Interface/IResourceManager.java
	jar cvf RMIInterface.jar Server/Interface/IResourceManager.class

compile-server-rmi: Server/RMI/*.java Server/Interface/IResourceManager.java Server/Common/*.java
	javac Server/RMI/*.java Server/Interface/IResourceManager.java Server/Common/*.java

compile-middleware: Middleware/*.java Server/Interface/IResourceManager.java Server/Common/*.java
	javac Middleware/*.java Server/Interface/IResourceManager.java Server/Common/*.java

clean:
	rm -f Server/Interface/*.class Server/Common/*.class Server/RMI/*.class Middleware/*.class
	rm -f RMIInterface.jar