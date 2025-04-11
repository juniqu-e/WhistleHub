"""
서버를 실행하는 파일
"""
from fastapi import FastAPI
import logging.config
import uvicorn
import utils
import config
from common.exceptions import add_exception_handler
import app.routers as routers

app = FastAPI()


@app.on_event("startup")
def startup_event():
    logging.config.dictConfig(config.LOG_CONFIG)
    utils.log("Server Start")


@app.on_event("shutdown")
def shutdown_event():
    utils.log("Server Shutdown")


add_exception_handler(app)
app.include_router(routers.track_router)

if __name__ == "__main__":
    uvicorn.run(
        app,
        host=config.FAST_API_HOST,
        port=config.FAST_API_PORT,
    )
