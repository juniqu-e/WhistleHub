from pydantic import BaseModel
from typing import List

class SimilarityResetRequest(BaseModel):
    trackIds: List[int]