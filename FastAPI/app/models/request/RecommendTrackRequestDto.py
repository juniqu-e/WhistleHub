from pydantic import BaseModel
from typing import List

class RecommendTrackRequest(BaseModel):
    needInstrumentTypes: List[int]
    trackIds: List[int]