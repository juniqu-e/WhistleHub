from pydantic import BaseModel, Field, RootModel
from typing import List, Optional, Dict

class SimilarityResetResponse(BaseModel):
    """
    RecommendTrackResponse
    추천 트랙 응답 Dto

    Attributes:
    ---
    trackId: int
        트랙 ID
    similarity: float
        유사도 점수
    """
    trackId: int = Field(..., title="트랙 ID", description="트랙 ID")
    similarity: float = Field(..., title="유사도 점수", description="유사도 점수")


class SimilarityResetResponseDto(RootModel):
    """
    SimilarityResetResponseDto
    유사도 초기화 응답 Dto

    Attributes:
    ---
        {
            "1":
            [
                {
                    "trackId" : 1,
                    "similarity" : 1.0
                },
                // ...existing code...
            ],
            "2":
            [
                {
                    "trackId" : 1,
                    "similarity" : 1.0
                },
                // ...existing code...
            ],
            // ...existing code...
        }
    """
    root: Dict[str, List[SimilarityResetResponse]]