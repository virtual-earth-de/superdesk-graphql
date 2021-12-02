CLIENT_ID=61a2666419b8361ae639d5f9
CLIENT_SECRET=5t9O24UTTtT656cYfmNXXBr06fNIxSlVbZ0ajO3o
#URL=https://superdesk.literatur.review/api/auth_server/token
URL=http://localhost:8082
echo curl -u ${CLIENT_ID}:${CLIENT_SECRET} -XPOST ${URL} -F grant_type=client_credentials
#curl --trace-ascii trace.out -u ${CLIENT_ID}:${CLIENT_SECRET} -XPOST ${URL} -F grant_type=client_credentials
curl --trace-ascii trace.out -u ${CLIENT_ID}:${CLIENT_SECRET} -H "Content-Type: application/x-www-form-urlencoded" -XPOST ${URL} -d "grant_type=client_credentials" 

